package main.ImageRenderer;

import DAO.Entities.Country;
import main.Chuu;
import main.Commands.CommandUtil;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;


public class WorldMapRenderer {


	private final static String MAP_FLAT = "BlankMap-World6-Equirectangular.svg";
	private final static String MAP_ROUND = "images/BlankMap-World.svg";
	private final static String SVG_NAMESPACE_URI = "";
	private final static List<String[]> palettes = initializePallete();
	private final static String[] textDescArray = new String[]{"text2527", "text2525", "text2523", "text2521", "text2519", "text2517", "text2539"};
	private final static String[] colourArray = new String[]{"rect6278", "rect6280", "rect6282", "rect6284", "rect6286", "rect6288"};


	//http://colorbrewer2.org
	private static List<String[]> initializePallete() {
		List<String[]> list = new ArrayList<>();
		list.add(new String[]{"rgb(237,248,251)", "rgb(204,236,230)", "rgb(153,216,201)", "rgb(102,194,164)", "rgb(44,162,95)", "rgb(0,109,44)"});
		list.add(new String[]{"rgb(255,255,178)", "rgb(254,217,118)", "rgb(254,178,76)", "rgb(253,141,60)", "rgb(240,59,32)", "rgb(189,0,38)"});
		list.add(new String[]{"rgb(254,235,226)", "rgb(252,197,192)", "rgb(250,159,181)", "rgb(247,104,161)", "rgb(197,27,138)", "rgb(122,1,119)"});
		list.add(new String[]{"rgb(254,229,217)", "rgb(252,187,161)", "rgb(252,146,114)", "rgb(251,106,74)", "rgb(222,45,38)", "rgb(165,15,21)"});
		return list;
	}

	public static byte[] generateImage(Map<Country, Integer> countryFrequency) {

		// make a Document with the base map

		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		Document doc;
		try {
			doc = f.createDocument("src/main/resources/BlankMap-World.svg",
					WorldMapRenderer.class.getClassLoader().getResourceAsStream(MAP_ROUND));
		} catch (IOException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			return null;
		}

		// prepare to modify and transcode the document

		// find the existing stylesheet in the document
		NodeList stylesList = doc.getElementsByTagName("style");
		Node styleNode = stylesList.item(0);
		String value = styleNode.getFirstChild().getNodeValue();
		StringBuilder sb = new StringBuilder();
		String[] palette = palettes.get(new Random().nextInt(palettes.size()));
		Optional<Integer> max = countryFrequency.values().stream().max(Integer::compareTo);
		if (!max.isPresent())
			return null;

		Integer[] range = initRange(max.get(), palette.length);

		initColours(range, palette, sb);
		initLegendText(range, doc, countryFrequency.size());
		countryFrequency.forEach(
				(country, integer) -> {
					String colorr = index(integer, range, palette);
					sb.append("\n .").append(country.getCountryCode().toLowerCase()).append("{fill: ")
							.append(colorr)
							.append(";}\n");
				}
		);

		value += (sb);
		styleNode.getFirstChild().setNodeValue(value);

		TranscoderInput input = new TranscoderInput(doc);
		PNGTranscoder s = new PNGTranscoder();
		s.addTranscodingHint(PNGTranscoder.KEY_BACKGROUND_COLOR, Color.white);
		//PNGTranscoder t = new PNGTranscoder();
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		TranscoderOutput output = new TranscoderOutput(ostream);
		try {
			s.transcode(input, output);
			return ostream.toByteArray();
		} catch (Exception e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			return null;
		}
	}


	//From bigger to smaller
	private static Integer[] initRange(int max, int length) {
		Integer[] returnedArray = new Integer[length];
		int initValue = 1;

		returnedArray[0] = initValue;
		for (int i = 0; i < length - 1; i++) {
			returnedArray[i + 1] = Math.max((int) Math.pow(Math.E, (Math.log(max) / 6) * ((i + 1))), i + 2);
		}

		ArrayUtils.reverse(returnedArray);
		return returnedArray;
	}

	private static void initColours(Integer[] range, String[] palette, StringBuilder sb) {
		//Legend Ids from Top to bottom

		for (int i = 0; i < colourArray.length; i++) {
			sb.append("\n #").append(colourArray[i]).append("{fill: ").append(palette[colourArray.length - 1 - i])
					.append(";}\n");
		}

	}

	private static String index(int plays, Integer[] range, String[] pallete) {
		int i = range.length - 1;
		while (plays > range[i] && i > 0) {
			i--;
		}
		return pallete[range.length - 1 - i];

	}

	private static void initLegendText(Integer[] range, Document doc, int totalCountries) {
		//Bottom to top
		Element elementById = doc.getElementById(textDescArray[0]);
		elementById.getFirstChild()
				.setNodeValue("> " + range[0] + CommandUtil.singlePlural(range[0], " Artist", " Artists"));

		for (int i = 1; i < textDescArray.length - 1; i++) {
			int previous = range[i - 1];
			elementById = doc.getElementById(textDescArray[i]);

			if (previous - 1 == range[i])
				elementById.getFirstChild()
						.setNodeValue(range[i] + CommandUtil.singlePlural(range[i], " Artist", " Artists"));
			else
				elementById.getFirstChild().setNodeValue((range[i]) + "-" + (previous - 1) + CommandUtil
						.singlePlural(range[i], " Artist", " Artists"));
		}
		elementById = doc.getElementById(textDescArray[6]);
		elementById.getFirstChild().setNodeValue("# Countries: " + totalCountries);

	}

}
