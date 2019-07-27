package main.ImageRenderer;

import DAO.Entities.Country;
import main.Commands.CommandUtil;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.Map;


public class WorldMapRenderer {


	private final static String MAP_FLAT = "BlankMap-World6-Equirectangular.svg";
	private final static String MAP_ROUND = "BlankMap-World.svg";
	private final static String SVG_NAMESPACE_URI = "";

	public static byte[] generateImage(Map<Country, Integer> countryFrequency) {
		try {
			// make a Document with the base map

			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			Document doc = f.createDocument("src/main/resources/BlankMap-World.svg",
					WorldMapRenderer.class.getClassLoader().getResourceAsStream(MAP_ROUND));

			// prepare to modify and transcode the document

			// find the existing stylesheet in the document
			NodeList stylesList = doc.getElementsByTagName("style");
			Node styleNode = stylesList.item(0);
			String value = styleNode.getFirstChild().getNodeValue();
			StringBuilder sb = new StringBuilder();
			countryFrequency.forEach(
					(country, integer) -> {
						Color color = CommandUtil.randomColor();
						String colorr = "rgb(" + color.getRed() + "," + color.getBlue() + "," + color.getGreen() + ")";
						sb.append("\n .").append(country.getCountryCode().toLowerCase()).append("{fill: ")
								.append(colorr)
								.append(";}\n");
					}
			);

			value += (sb);
			styleNode.getFirstChild().setNodeValue(value);

			PNGTranscoder t = new PNGTranscoder();
			TranscoderInput input = new TranscoderInput(doc);
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();

			TranscoderOutput output = new TranscoderOutput(ostream);
			t.transcode(input, output);
			return ostream.toByteArray();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}


}
