package core.imagerenderer;

import core.Chuu;
import core.commands.CommandUtil;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;

public class CircleRenderer {

    private static final String MAP_ROUND = "images/Clock.svg";
    private static final String[] textDescArray = new String[]{"text2527", "text2525", "text2523", "text2521", "text2519", "text2517", "text2539"};
    private static final String[] colourArray = new String[]{"rect6278", "rect6280", "rect6282", "rect6284", "rect6286", "rect6288"};

    private CircleRenderer() {
    }


    public static byte[] generateImage(Map<Integer, Long> hourFrequency, Integer key) {

        // make a Document with the base map

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        Document doc;
        try {
            doc = f.createDocument("t.svg",
                    WorldMapRenderer.class.getClassLoader().getResourceAsStream(MAP_ROUND));
        } catch (IOException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            return null;
        }


        // prepare to modify and transcode the document

        // find the existing stylesheet in the document
        long i = hourFrequency.values().stream().mapToLong(x -> x).max().orElse(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");


        int week = key % 100;
        int year = (key - (week)) / 1000;
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        LocalDateTime ldt = LocalDateTime.now()
                .withYear(year)
                .with(weekFields.weekOfYear(), week)
                .with(weekFields.dayOfWeek(), 1);

        String one = formatter.format(ldt.toLocalDate());
        String dayOne = String.format("%02d", ldt.getDayOfMonth()) + CommandUtil.getDayNumberSuffix(ldt.getDayOfMonth());
        LocalDate temporal = ldt.plus(1, ChronoUnit.WEEKS).toLocalDate();
        String second = formatter.format(temporal);
        String daySecond = String.format("%02d", temporal.getDayOfMonth()) + CommandUtil.getDayNumberSuffix(temporal.getDayOfMonth());
        String s1 = dayOne + " " + one + " - " + daySecond + " " + second;
        doc.getElementById("lpm").setTextContent(s1);
        for (Map.Entry<Integer, Long> integerIntegerEntry : hourFrequency.entrySet()) {
            Integer hour = integerIntegerEntry.getKey();
            Long count = integerIntegerEntry.getValue();
            doc.getChildNodes().item(1);
            double ratio = 55 + (((double) count) / i) * 65;
            String id = String.format("%02d", hour);
            Element elementById = doc.getElementById(id);
            Node item = elementById.getChildNodes().item(3);
            Node r = item.getAttributes().getNamedItem("r");
            r.setNodeValue(String.valueOf(ratio));
        }


        TranscoderInput input = new TranscoderInput(doc);
//        In Apache Batik, you can change resolution by passing Transcoder hint KEY_PIXEL_UNIT_TO_MILLIMETER.
//
//        However the important aspect is that you need to scale your height & width to same scale as the new resolution you are seeking.
//
//                For example:
//
//        my SVG has 3.5 * 2.0 Inches (252 * 144 Pixels) size.
//

        int RESOLUTION_DPI = 600;
        float SCALE_BY_RESOLUTION = RESOLUTION_DPI / 72f;
        float scaledWidth = 240 * SCALE_BY_RESOLUTION;
        float scaledHeight = 260 * SCALE_BY_RESOLUTION;
        float pixelUnitToMM = 25.4f / RESOLUTION_DPI;
        PNGTranscoder s = new PNGTranscoder();
        s.addTranscodingHint(PNGTranscoder.KEY_BACKGROUND_COLOR, Color.white);
        s.addTranscodingHint(ImageTranscoder.KEY_BACKGROUND_COLOR, Color.WHITE);
        s.addTranscodingHint(PNGTranscoder.KEY_WIDTH, scaledWidth);
        s.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, scaledHeight);
        s.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, pixelUnitToMM);
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(ostream);


        try {
            DOMSource source = new DOMSource(doc);

            FileWriter writer = new FileWriter(new File("C:\\Users\\ish\\Documents\\ttest\\" + UUID.randomUUID().toString()));
            StreamResult result = new StreamResult(writer);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
            s.transcode(input, output);
            return ostream.toByteArray();
        } catch (Exception e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            return null;
        }
    }


}
