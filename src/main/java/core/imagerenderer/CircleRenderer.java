package core.imagerenderer;

import core.Chuu;
import core.commands.CommandUtil;
import core.services.ClockService;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class CircleRenderer {

    private static final String CLOCK_SVG = "images/Clock.svg";

    private CircleRenderer() {
    }


    public static byte[] generateImage(ClockService.ClockMode clockMode, Map<Integer, Long> hourFrequency, Integer key, TimeZone timeZone) {

        // make a Document with the base map

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        Document doc;
        try {
            doc = f.createDocument("t.svg",
                    WorldMapRenderer.class.getClassLoader().getResourceAsStream(CLOCK_SVG));
        } catch (IOException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            return null;
        }


        // prepare to modify and transcode the document

        // find the existing stylesheet in the document
        long i = hourFrequency.values().stream().mapToLong(x -> x).max().orElse(0);


        String s1;
        if (clockMode.equals(ClockService.ClockMode.BY_WEEK)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            int week = key % 100;
            int year = (key - (week)) / 1000;
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            ZonedDateTime ldt = Instant.now().atZone(timeZone.toZoneId())
                    .withYear(year)
                    .with(weekFields.weekOfYear(), week)
                    .with(weekFields.dayOfWeek(), 1);
            String one = formatter.format(ldt.toLocalDate());
            String dayOne = String.format("%02d", ldt.getDayOfMonth()) + CommandUtil.getDayNumberSuffix(ldt.getDayOfMonth());
            LocalDate temporal = ldt.plus(1, ChronoUnit.WEEKS).toLocalDate();
            String second = formatter.format(temporal);
            String daySecond = String.format("%02d", temporal.getDayOfMonth()) + CommandUtil.getDayNumberSuffix(temporal.getDayOfMonth());
            s1 = dayOne + " " + one + " - " + daySecond + " " + second;
        } else {
            int dayOfweek = key;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");

            ZonedDateTime ldt = LocalDateTime.now().atZone(timeZone.toZoneId())
                    .withDayOfYear(dayOfweek)
                    .withYear(Year.now().getValue());
            String day = formatter.format(ldt.toLocalDate());
            String month = DateTimeFormatter.ofPattern("MMM").format(ldt.toLocalDate());
            s1 = month + " " + day + CommandUtil.getDayNumberSuffix(Integer.parseInt(day));
        }
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

        int RESOLUTION_DPI = 72;
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
