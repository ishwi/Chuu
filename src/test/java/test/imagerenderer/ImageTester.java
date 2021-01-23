package test.imagerenderer;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.PreBillboardUserDataTimestamped;
import dao.entities.ScrobbledArtist;
import dao.entities.Track;
import dao.entities.TrackWithArtistId;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static core.Chuu.readToken;

public class ImageTester {
    private static DiscogsApi discogsApi;
    private static Spotify spotify;
    private final Map<String, Long> dbIdMap = new HashMap<>();
    private static ChuuService service;
    private static final ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();

    TemporalField weekOfYear = WeekFields.of(Locale.getDefault()).weekOfYear();

    @BeforeClass
    public static void beforeClass() throws Exception {
        Properties properties = readToken();
        service = new ChuuService();
        DiscogsSingleton.init(properties.getProperty("DC_SC"), properties.getProperty("DC_KY"));
        SpotifySingleton.init(properties.getProperty("client_ID"), properties.getProperty("client_Secret"));
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotify = SpotifySingleton.getInstance();
    }

    public List<PreBillboardUserDataTimestamped> fromCsv() {
        String csvFile = "C:\\Users\\ish\\Downloads\\lukyfan_4.csv";
        String line = "";
        String cvsSplitBy = ",";
        List<PreBillboardUserDataTimestamped> a = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        LongAdder s = new LongAdder();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] country = line.split(cvsSplitBy);
                int length = country.length;
                LocalDateTime parse;
                try {
                    parse = dateTimeFormatter.parse(country[length - 1], LocalDateTime::from);
                } catch (Exception e) {
                    //      System.out.println("");
                    continue;
                }
                ZoneOffset of = ZoneOffset.of("+01:00");
                Timestamp from = Timestamp.from(parse.toInstant(of));
                s.increment();
                a.add(new PreBillboardUserDataTimestamped(s.longValue(), "vbooy", s.toString(), 1, from));
                //   System.out.println("Country [code= " + country[2] + " , name=   " + country[3] + "]");

            }

        } catch (IOException e) {
            //e.printStackTrace();
        }
        return a;

    }

    @Test
    public void name() throws IOException, ClassNotFoundException {
        boolean getData = false;
        if (getData) {
            a();
        }
/*        // List<PreBillboardUserDataTimestamped> ishwaracoello = (List<PreBillboardUserDataTimestamped>) objectinputstream.readObject();
//        List<PreBillboardUserDataTimestamped> ishwaracoello = service.getUngroupedUserData(2, "ishwaracoello");*/
//        List<PreBillboardUserDataTimestamped> ishwaracoello = fromCsv();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ImageOutputStream output = ImageIO.createImageOutputStream(baos);
//
//        GifSequenceWriter writer =
//                new GifSequenceWriter(output, BufferedImage.TYPE_4BYTE_ABGR, 20, false);
//        HashMap<Integer, List<PreBillboardUserDataTimestamped>> collect = ishwaracoello.stream().
//                collect(Collectors.groupingBy(x -> {
//                    int i = x.getTimestamp().toLocalDateTime().toLocalDate().get(weekOfYear);
//                    int y = x.getTimestamp().toLocalDateTime().toLocalDate().getYear();
//
//                    return y * 1000 + i;
//                }, HashMap::new, Collectors.toList()));
//        collect.entrySet().parallelStream().sorted(Map.Entry.comparingByKey()).forEachOrdered(
//                t -> {
//                    Integer key = t.getKey();
//                    List<PreBillboardUserDataTimestamped> value = t.getValue();
//                    HashMap<Integer, Long> collect1 = value.stream().collect(Collectors.groupingBy(x -> x.getTimestamp().toLocalDateTime().getHour(), HashMap::new, Collectors.counting()));
//                    byte[] bytes = CircleRenderer.generateImage(ClockService.ClockMode.BY_DAY, collect1, key);
//                    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//                    try {
//                        BufferedImage read = ImageIO.read(bais);
//                        Graphics2D g = read.createGraphics();
//                        System.out.println(read.getType());
//                        GraphicUtils.setQuality(g);
//                        g.dispose();
//                        writer.writeToSequence(read);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//        );


        // create a new BufferedOutputStream with the last argument

        // create a gif sequence with the type of the first image, 1 second
        // between frames, which loops continuously


//        // write out the first image to our sequence...
//
//        writer.close();
//        output.close();
//        byte[] bytes = baos.toByteArray();
//        FileUtils.writeByteArrayToFile(File.createTempFile(UUID.randomUUID().toString(), ".gif"), bytes);
//

    }

    private void image(List<PreBillboardUserDataTimestamped> t) {

    }

    private void a() {
        Date weekStart = Date.valueOf("2017-01-01");

        LocalDateTime weekBeginning = weekStart.toLocalDate().atStartOfDay();

        try {
            String lastFMName = "ishwaracoello";
            List<TrackWithArtistId> tracksAndTimestamps = lastFM.getWeeklyBillboard(lastFMName,
                    (int) weekBeginning.toEpochSecond(OffsetDateTime.now().getOffset())
                    , (int) LocalDateTime.now().toEpochSecond(OffsetDateTime.now().getOffset()));

            doArtistValidation(tracksAndTimestamps);
            service.insertUserData(2, lastFMName, tracksAndTimestamps);

        } catch (LastFmException ignored) {
        }
    }

    private void doArtistValidation(List<TrackWithArtistId> toValidate) {
        toValidate = toValidate.stream().peek(x -> {
            Long aLong1 = dbIdMap.get(x.getArtist());
            if (aLong1 != null)
                x.setArtistId(aLong1);
        }).filter(x -> x.getArtistId() == -1L || x.getAlbumId() == 0L).collect(Collectors.toList());

        List<ScrobbledArtist> artists = toValidate.stream().map(Track::getArtist).distinct().map(x -> new ScrobbledArtist(x, 0, null)).collect(Collectors.toList());
        service.filldArtistIds(artists);
        Map<Boolean, List<ScrobbledArtist>> collect1 = artists.stream().collect(Collectors.partitioningBy(x -> x.getArtistId() != -1L && x.getArtistId() != 0));
        List<ScrobbledArtist> foundArtists = collect1.get(true);
        Map<String, String> changedUserNames = new HashMap<>();
        collect1.get(false).stream().map(x -> {
            try {
                String artist = x.getArtist();
                CommandUtil.validate(service, x, lastFM, discogsApi, spotify);
                String newArtist = x.getArtist();
                if (!Objects.equals(artist, newArtist)) {
                    changedUserNames.put(artist, newArtist);
                }
                return x;
            } catch (LastFmException lastFmException) {
                return null;
            }
        }).filter(Objects::nonNull).forEach(foundArtists::add);
        Map<String, Long> mapId = foundArtists.stream().collect(Collectors.toMap(ScrobbledArtist::getArtist, ScrobbledArtist::getArtistId, (x, y) -> x));

        for (Iterator<TrackWithArtistId> iterator = toValidate.iterator(); iterator.hasNext(); ) {
            TrackWithArtistId x = iterator.next();
            Long aLong = mapId.get(x.getArtist());
            if (aLong == null) {
                String s = changedUserNames.get(x.getArtist());
                if (s != null) {
                    aLong = mapId.get(s);
                }
                if (aLong == null) {
                    aLong = -1L;
                }
            }
            x.setArtistId(aLong);
            if (x.getArtistId() == -1L) {
                iterator.remove();
            }
            this.dbIdMap.put(x.getArtist(), x.getArtistId());
        }
    }

    @Test
    public void nam2e() {
        String[] split = svg.split("\n");
        StringBuilder str = new StringBuilder();
        int counter = 0;
        for (String s : split) {
            String trim = s.trim();
            int counter2 = 0;
            if (trim.startsWith("<clipPath id")) {

                Pattern compile = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+([eE][\\-+]?[0-9]+)?)[, \"]");
                Matcher matcher = compile.matcher(trim);
                System.out.println(trim);
                int innerCounter = 0;
                while (matcher.find()) {
                    if (counter2++ < 2) {
                        continue;
                    }
                    String group = matcher.group(1);
                    double aDouble = Double.parseDouble(group);

                    trim = trim.replaceFirst(group, new DecimalFormat("#.###########################################").format(aDouble * 4)); //"1.2"

                }
                System.out.println(trim);
                System.out.println();

            }
            str.append(trim).append("\n");

        }
        System.out.println(str.toString());
    }

    private final String svg = "<!DOCTYPE svg [\n" +
            "        <!ELEMENT svg (defs|g|svg|style)*>\n" +
            "        <!ATTLIST svg\n" +
            "                class CDATA #IMPLIED\n" +
            "                height CDATA #IMPLIED\n" +
            "                id CDATA #REQUIRED\n" +
            "                style CDATA #IMPLIED\n" +
            "                version CDATA #IMPLIED\n" +
            "                viewBox CDATA #IMPLIED\n" +
            "                width CDATA #IMPLIED\n" +
            "                x CDATA #IMPLIED\n" +
            "                xmlns CDATA #IMPLIED\n" +
            "                xmlns:svgjs CDATA #IMPLIED\n" +
            "                xmlns:xlink CDATA #IMPLIED\n" +
            "                y CDATA #IMPLIED>\n" +
            "        <!ELEMENT defs (clipPath)*>\n" +
            "        <!ATTLIST defs\n" +
            "                id CDATA #REQUIRED>\n" +
            "        <!ELEMENT clipPath (polygon|path)*>\n" +
            "        <!ATTLIST clipPath\n" +
            "                id CDATA #REQUIRED>\n" +
            "        <!ELEMENT polygon (#PCDATA)>\n" +
            "        <!ATTLIST polygon\n" +
            "                fill CDATA #REQUIRED\n" +
            "                id CDATA #REQUIRED\n" +
            "                points CDATA #REQUIRED\n" +
            "                stroke CDATA #IMPLIED\n" +
            "                stroke-linejoin CDATA #IMPLIED\n" +
            "                stroke-width CDATA #IMPLIED>\n" +
            "        <!ELEMENT path (#PCDATA)>\n" +
            "        <!ATTLIST path\n" +
            "                class CDATA #IMPLIED\n" +
            "                d CDATA #REQUIRED\n" +
            "                fill CDATA #IMPLIED\n" +
            "                id CDATA #REQUIRED>\n" +
            "        <!ELEMENT g (g|circle|line|polygon|text|svg|path)*>\n" +
            "        <!ATTLIST g\n" +
            "                clip-path CDATA #IMPLIED\n" +
            "                data-data CDATA #IMPLIED\n" +
            "                id CDATA #REQUIRED\n" +
            "                stroke CDATA #IMPLIED\n" +
            "                stroke-width CDATA #IMPLIED\n" +
            "                transform CDATA #IMPLIED>\n" +
            "        <!ELEMENT circle (#PCDATA)>\n" +
            "        <!ATTLIST circle\n" +
            "                class CDATA #IMPLIED\n" +
            "                cx CDATA #REQUIRED\n" +
            "                cy CDATA #REQUIRED\n" +
            "                fill CDATA #REQUIRED\n" +
            "                fill-opacity CDATA #IMPLIED\n" +
            "                id ID #REQUIRED\n" +
            "                r CDATA #REQUIRED>\n" +
            "        <!ELEMENT line (#PCDATA)>\n" +
            "        <!ATTLIST line\n" +
            "                id CDATA #REQUIRED\n" +
            "                style CDATA #REQUIRED\n" +
            "                x1 CDATA #REQUIRED\n" +
            "                x2 CDATA #REQUIRED\n" +
            "                y1 CDATA #REQUIRED\n" +
            "                y2 CDATA #REQUIRED>\n" +
            "        <!ELEMENT text (#PCDATA)>\n" +
            "        <!ATTLIST text\n" +
            "                fill CDATA #REQUIRED\n" +
            "                font-family CDATA #REQUIRED\n" +
            "                font-size CDATA #REQUIRED\n" +
            "                font-weight CDATA #REQUIRED\n" +
            "                id ID #REQUIRED\n" +
            "                x CDATA #REQUIRED\n" +
            "                y CDATA #REQUIRED>\n" +
            "        <!ELEMENT style (#PCDATA)>\n" +
            "        ]>\n" +
            "<svg id=\"SvgjsSvg1021\" width=\"240\" height=\"260\"\n" +
            "     xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"\n" +
            "     xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
            "     xmlns:svgjs=\"http://svgjs.com/svgjs\" viewBox=\"0 0 240 260\" class=\"user-dashboard-module-svg\">\n" +
            "    <defs id=\"SvgjsDefs1022\">\n" +
            "        <clipPath id=\"SvgjsClipPath1028\"><polygon id=\"SvgjsPolygon1025\" points=\"0,0 -4.408728476930471e-14,-240 62.11657082460487,-231.82219830937643\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1033\"><polygon id=\"SvgjsPolygon1030\" points=\"0,0 62.11657082460487,-231.82219830937643 120.00000000000003,-207.84609690826525\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1038\"><polygon id=\"SvgjsPolygon1035\" points=\"0,0 120.00000000000003,-207.84609690826525 169.70562748477136,-169.70562748477144\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1043\"><polygon id=\"SvgjsPolygon1040\" points=\"0,0 169.70562748477136,-169.70562748477144 207.84609690826522,-120.00000000000011\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1048\"><polygon id=\"SvgjsPolygon1045\" points=\"0,0 207.84609690826522,-120.00000000000011 231.8221983093764,-62.11657082460496\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1053\"><polygon id=\"SvgjsPolygon1050\" points=\"0,0 231.8221983093764,-62.11657082460496 240,-5.878304635907296e-14\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1058\"><polygon id=\"SvgjsPolygon1055\" points=\"0,0 240,-5.878304635907296e-14 231.82219830937643,62.11657082460486\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1063\"><polygon id=\"SvgjsPolygon1060\" points=\"0,0 231.82219830937643,62.11657082460486 207.84609690826537,119.99999999999983\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1068\"><polygon id=\"SvgjsPolygon1065\" points=\"0,0 207.84609690826537,119.99999999999983 169.70562748477144,169.70562748477136\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1073\"><polygon id=\"SvgjsPolygon1070\" points=\"0,0 169.70562748477144,169.70562748477136 119.99999999999993,207.8460969082653\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1078\"><polygon id=\"SvgjsPolygon1075\" points=\"0,0 119.99999999999993,207.8460969082653 62.11657082460498,231.8221983093764\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1083\"><polygon id=\"SvgjsPolygon1080\" points=\"0,0 62.11657082460498,231.8221983093764 7.347880794884119e-14,240\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1088\"><polygon id=\"SvgjsPolygon1085\" points=\"0,0 7.347880794884119e-14,240 -62.11657082460484,231.82219830937643\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1093\"><polygon id=\"SvgjsPolygon1090\" points=\"0,0 -62.11657082460484,231.82219830937643 -120.00000000000018,207.8460969082652\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1098\"><polygon id=\"SvgjsPolygon1095\" points=\"0,0 -120.00000000000018,207.8460969082652 -169.7056274847712,169.7056274847716\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1103\"><polygon id=\"SvgjsPolygon1100\" points=\"0,0 -169.7056274847712,169.7056274847716 -207.8460969082653,119.99999999999994\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1108\"><polygon id=\"SvgjsPolygon1105\" points=\"0,0 -207.8460969082653,119.99999999999994 -231.8221983093764,62.11657082460499\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1113\"><polygon id=\"SvgjsPolygon1110\" points=\"0,0 -231.8221983093764,62.11657082460499 -240,8.817456953860942e-14\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1118\"><polygon id=\"SvgjsPolygon1115\" points=\"0,0 -240,8.817456953860942e-14 -231.82219830937643,-62.11657082460483\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1123\"><polygon id=\"SvgjsPolygon1120\" points=\"0,0 -231.82219830937643,-62.11657082460483 -207.8460969082654,-119.9999999999998\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1128\"><polygon id=\"SvgjsPolygon1125\" points=\"0,0 -207.8460969082654,-119.9999999999998 -169.7056274847713,-169.7056274847715\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1133\"><polygon id=\"SvgjsPolygon1130\" points=\"0,0 -169.7056274847713,-169.7056274847715 -120.00000000000031,-207.84609690826508\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1138\"><polygon id=\"SvgjsPolygon1135\" points=\"0,0 -120.00000000000031,-207.84609690826508 -62.11657082460502,-231.82219830937638\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1143\"><polygon id=\"SvgjsPolygon1140\" points=\"0,0 -62.11657082460502,-231.82219830937638 -1.0287033112837766e-13,-240\" fill=\"#ffffff\"></polygon></clipPath>\n" +
            "        <clipPath id=\"SvgjsClipPath1173\"><path id=\"SvgjsPath1172\" d=\"M0 0L-4.408728476930471e-14 -240A240 240 0 0 1 7.347880794884119e-14 240A240 240 0 0 1 -1.0287033112837766e-13 -240Z \" fill=\"#ffffff\"></path></clipPath>\n" +
            "    </defs>\n" +
            "    <g id=\"SvgjsG1023\" transform=\"matrix(1,0,0,1,120,120)\">\n" +
            "        <g id=\"00\" clip-path='url(\"#SvgjsClipPath1028\")' data-data='{\"scrobbleCount\":3,\"listeningClockTooltip\":\"3 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1026\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1027\" r=\"0\" cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"01\" clip-path='url(\"#SvgjsClipPath1033\")' data-data='{\"scrobbleCount\":15,\"listeningClockTooltip\":\"15 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1031\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1032\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"02\" clip-path='url(\"#SvgjsClipPath1038\")' data-data='{\"scrobbleCount\":5,\"listeningClockTooltip\":\"5 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1036\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1037\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"03\" clip-path='url(\"#SvgjsClipPath1043\")' data-data='{\"scrobbleCount\":4,\"listeningClockTooltip\":\"4 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1041\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1042\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"04\" clip-path='url(\"#SvgjsClipPath1048\")' data-data='{\"scrobbleCount\":0,\"listeningClockTooltip\":\"0 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1046\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1047\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"05\" clip-path='url(\"#SvgjsClipPath1053\")' data-data='{\"scrobbleCount\":1,\"listeningClockTooltip\":\"1 scrobble\"}'>\n" +
            "            <circle id=\"SvgjsCircle1051\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1052\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"06\" clip-path='url(\"#SvgjsClipPath1058\")' data-data='{\"scrobbleCount\":0,\"listeningClockTooltip\":\"0 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1056\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1057\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"07\" clip-path='url(\"#SvgjsClipPath1063\")' data-data='{\"scrobbleCount\":0,\"listeningClockTooltip\":\"0 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1061\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1062\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"08\" clip-path='url(\"#SvgjsClipPath1068\")' data-data='{\"scrobbleCount\":0,\"listeningClockTooltip\":\"0 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1066\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1067\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"09\" clip-path='url(\"#SvgjsClipPath1073\")' data-data='{\"scrobbleCount\":0,\"listeningClockTooltip\":\"0 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1071\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1072\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"10\" clip-path='url(\"#SvgjsClipPath1078\")' data-data='{\"scrobbleCount\":3,\"listeningClockTooltip\":\"3 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1076\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1077\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"11\" clip-path='url(\"#SvgjsClipPath1083\")' data-data='{\"scrobbleCount\":27,\"listeningClockTooltip\":\"27 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1081\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1082\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"12\" clip-path='url(\"#SvgjsClipPath1088\")' data-data='{\"scrobbleCount\":34,\"listeningClockTooltip\":\"34 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1086\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1087\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"13\" clip-path='url(\"#SvgjsClipPath1093\")' data-data='{\"scrobbleCount\":51,\"listeningClockTooltip\":\"51 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1091\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1092\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"14\" clip-path='url(\"#SvgjsClipPath1098\")' data-data='{\"scrobbleCount\":48,\"listeningClockTooltip\":\"48 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1096\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1097\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"15\" clip-path='url(\"#SvgjsClipPath1103\")' data-data='{\"scrobbleCount\":48,\"listeningClockTooltip\":\"48 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1101\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1102\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"16\" clip-path='url(\"#SvgjsClipPath1108\")' data-data='{\"scrobbleCount\":18,\"listeningClockTooltip\":\"18 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1106\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1107\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"17\" clip-path='url(\"#SvgjsClipPath1113\")' data-data='{\"scrobbleCount\":16,\"listeningClockTooltip\":\"16 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1111\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1112\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"18\" clip-path='url(\"#SvgjsClipPath1118\")' data-data='{\"scrobbleCount\":18,\"listeningClockTooltip\":\"18 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1116\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1117\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"19\" clip-path='url(\"#SvgjsClipPath1123\")' data-data='{\"scrobbleCount\":14,\"listeningClockTooltip\":\"14 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1121\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1122\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"20\" clip-path='url(\"#SvgjsClipPath1128\")' data-data='{\"scrobbleCount\":9,\"listeningClockTooltip\":\"9 scrobbles\"}'>\n" +
            "            <circle id=\"SvgjsCircle1126\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1127\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"21\" clip-path='url(\"#SvgjsClipPath1133\")' data-data='{\"scrobbleCount\":1,\"listeningClockTooltip\":\"1 scrobble\"}'>\n" +
            "            <circle id=\"SvgjsCircle1131\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1132\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"22\" clip-path='url(\"#SvgjsClipPath1138\")' data-data='{\"scrobbleCount\":1,\"listeningClockTooltip\":\"1 scrobble\"}'>\n" +
            "            <circle id=\"SvgjsCircle1136\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1137\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"23\" clip-path='url(\"#SvgjsClipPath1143\")' data-data='{\"scrobbleCount\":1,\"listeningClockTooltip\":\"1 scrobble\"}'>\n" +
            "            <circle id=\"SvgjsCircle1141\" r=\"120\" cx=\"0\" cy=\"0\" fill-opacity=\"0.2\" fill=\"#0066ff\"></circle>\n" +
            "            <circle id=\"SvgjsCircle1142\" r=\"0\"  cx=\"0\" cy=\"0\" fill=\"#0066ff\" class=\"segment-value\"></circle>\n" +
            "        </g>\n" +
            "        <g id=\"SvgjsG1144\" stroke=\"#ffffff\" stroke-width=\"2\">\n" +
            "            <line id=\"SvgjsLine1145\" x1=\"0\" y1=\"0\" x2=\"130\" y2=\"0\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1146\" x1=\"0\" y1=\"0\" x2=\"125.57035741757888\" y2=\"33.646475863327694\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1147\" x1=\"0\" y1=\"0\" x2=\"112.58330249197704\" y2=\"64.99999999999999\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1148\" x1=\"0\" y1=\"0\" x2=\"91.92388155425118\" y2=\"91.92388155425117\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1149\" x1=\"0\" y1=\"0\" x2=\"65.00000000000001\" y2=\"112.58330249197702\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1150\" x1=\"0\" y1=\"0\" x2=\"33.646475863327694\" y2=\"125.57035741757888\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1151\" x1=\"0\" y1=\"0\" x2=\"7.960204194457796e-15\" y2=\"130\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1152\" x1=\"0\" y1=\"0\" x2=\"-33.64647586332771\" y2=\"125.57035741757888\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1153\" x1=\"0\" y1=\"0\" x2=\"-64.99999999999997\" y2=\"112.58330249197704\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1154\" x1=\"0\" y1=\"0\" x2=\"-91.92388155425117\" y2=\"91.92388155425118\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1155\" x1=\"0\" y1=\"0\" x2=\"-112.58330249197704\" y2=\"64.99999999999999\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1156\" x1=\"0\" y1=\"0\" x2=\"-125.57035741757886\" y2=\"33.64647586332773\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1157\" x1=\"0\" y1=\"0\" x2=\"-130\" y2=\"1.592040838891559e-14\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1158\" x1=\"0\" y1=\"0\" x2=\"-125.57035741757889\" y2=\"-33.646475863327645\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1159\" x1=\"0\" y1=\"0\" x2=\"-112.58330249197702\" y2=\"-65.00000000000001\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1160\" x1=\"0\" y1=\"0\" x2=\"-91.9238815542512\" y2=\"-91.92388155425117\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1161\" x1=\"0\" y1=\"0\" x2=\"-65.00000000000006\" y2=\"-112.58330249197701\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1162\" x1=\"0\" y1=\"0\" x2=\"-33.64647586332768\" y2=\"-125.57035741757888\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1163\" x1=\"0\" y1=\"0\" x2=\"-2.3880612583373385e-14\" y2=\"-130\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1164\" x1=\"0\" y1=\"0\" x2=\"33.64647586332764\" y2=\"-125.57035741757889\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1165\" x1=\"0\" y1=\"0\" x2=\"65.00000000000001\" y2=\"-112.58330249197702\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1166\" x1=\"0\" y1=\"0\" x2=\"91.92388155425115\" y2=\"-91.9238815542512\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1167\" x1=\"0\" y1=\"0\" x2=\"112.583302491977\" y2=\"-65.00000000000006\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1168\" x1=\"0\" y1=\"0\" x2=\"125.57035741757888\" y2=\"-33.64647586332769\" style=\"pointer-events: none;\"></line>\n" +
            "        </g>\n" +
            "        <circle id=\"SvgjsCircle1169\" r=\"56\" cx=\"0\" cy=\"0\" fill=\"#ffffff\"></circle>\n" +
            "        <g id=\"SvgjsG1170\" clip-path='url(\"#SvgjsClipPath1173\")'><polygon id=\"SvgjsPolygon1171\" points=\"0,0\" stroke-linejoin=\"round\" stroke=\"#1a1a1a\" stroke-width=\"2\" fill=\"none\"></polygon></g>\n" +
            "        <g id=\"SvgjsG1174\" stroke=\"#c2c2c2\" stroke-width=\"1\">\n" +
            "            <line id=\"SvgjsLine1175\" x1=\"49\" y1=\"0\" x2=\"53\" y2=\"0\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1176\" x1=\"47.33036548816435\" y1=\"12.682133210023515\" x2=\"51.19406879332062\" y2=\"13.717409390433598\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1177\" x1=\"42.4352447854375\" y1=\"24.499999999999996\" x2=\"45.899346400575254\" y2=\"26.499999999999996\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1178\" x1=\"34.64823227814083\" y1=\"34.648232278140824\" x2=\"37.47665940288702\" y2=\"37.476659402887016\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1179\" x1=\"24.500000000000007\" y1=\"42.43524478543749\" x2=\"26.500000000000007\" y2=\"45.89934640057525\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1180\" x1=\"12.682133210023515\" y1=\"47.33036548816435\" x2=\"13.717409390433598\" y2=\"51.19406879332062\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1181\" x1=\"3.0003846579110155e-15\" y1=\"49\" x2=\"3.245314017740486e-15\" y2=\"53\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1182\" x1=\"-12.682133210023522\" y1=\"47.33036548816435\" x2=\"-13.717409390433605\" y2=\"51.19406879332062\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1183\" x1=\"-24.49999999999999\" y1=\"42.4352447854375\" x2=\"-26.49999999999999\" y2=\"45.899346400575254\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1184\" x1=\"-34.648232278140824\" y1=\"34.64823227814083\" x2=\"-37.476659402887016\" y2=\"37.47665940288702\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1185\" x1=\"-42.4352447854375\" y1=\"24.499999999999996\" x2=\"-45.899346400575254\" y2=\"26.499999999999996\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1186\" x1=\"-47.33036548816434\" y1=\"12.68213321002353\" x2=\"-51.19406879332062\" y2=\"13.717409390433614\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1187\" x1=\"-49\" y1=\"6.000769315822031e-15\" x2=\"-53\" y2=\"6.490628035480972e-15\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1188\" x1=\"-47.330365488164354\" y1=\"-12.682133210023498\" x2=\"-51.194068793320625\" y2=\"-13.717409390433579\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1189\" x1=\"-42.43524478543749\" y1=\"-24.500000000000007\" x2=\"-45.89934640057525\" y2=\"-26.500000000000007\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1190\" x1=\"-34.64823227814084\" y1=\"-34.648232278140824\" x2=\"-37.47665940288703\" y2=\"-37.476659402887016\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1191\" x1=\"-24.50000000000002\" y1=\"-42.43524478543748\" x2=\"-26.500000000000025\" y2=\"-45.89934640057524\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1192\" x1=\"-12.68213321002351\" y1=\"-47.33036548816435\" x2=\"-13.717409390433593\" y2=\"-51.19406879332062\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1193\" x1=\"-9.001153973733046e-15\" y1=\"-49\" x2=\"-9.735942053221457e-15\" y2=\"-53\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1194\" x1=\"12.682133210023494\" y1=\"-47.330365488164354\" x2=\"13.717409390433575\" y2=\"-51.194068793320625\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1195\" x1=\"24.500000000000007\" y1=\"-42.43524478543749\" x2=\"26.500000000000007\" y2=\"-45.89934640057525\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1196\" x1=\"34.64823227814082\" y1=\"-34.64823227814084\" x2=\"37.47665940288701\" y2=\"-37.47665940288703\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1197\" x1=\"42.43524478543748\" y1=\"-24.50000000000002\" x2=\"45.89934640057523\" y2=\"-26.500000000000025\" style=\"pointer-events: none;\"></line>\n" +
            "            <line id=\"SvgjsLine1198\" x1=\"47.33036548816435\" y1=\"-12.682133210023514\" x2=\"51.19406879332062\" y2=\"-13.717409390433597\" style=\"pointer-events: none;\"></line>\n" +
            "        </g>\n" +
            "        <text id=\"SvgjsText1199\" font-family='\"Open Sans\", \"Lucida Grande\", \"Helvetica Neue\", Helvetica, Arial, sans-serif' font-size=\"14\" font-weight=\"bold\" fill=\"#1a1a1a\" x=\"-7.992187500000007\" y=\"-32.5703125\">00</text>\n" +
            "        <text id=\"SvgjsText1200\" font-family='\"Open Sans\", \"Lucida Grande\", \"Helvetica Neue\", Helvetica, Arial, sans-serif' font-size=\"14\" font-weight=\"bold\" fill=\"#1a1a1a\" x=\"30.0078125\" y=\"5.429687499999991\">06</text>\n" +
            "        <text id=\"SvgjsText1201\" font-family='\"Open Sans\", \"Lucida Grande\", \"Helvetica Neue\", Helvetica, Arial, sans-serif' font-size=\"14\" font-weight=\"bold\" fill=\"#1a1a1a\" x=\"-7.9921874999999885\" y=\"43.4296875\">12</text>\n" +
            "        <text id=\"SvgjsText1202\" font-family='\"Open Sans\", \"Lucida Grande\", \"Helvetica Neue\", Helvetica, Arial, sans-serif' font-size=\"14\" font-weight=\"bold\" fill=\"#1a1a1a\" x=\"-45.9921875\" y=\"5.429687500000014\">18</text>\n" +
            "      \n" +
            "        <svg id=\"SvgjsSvg1203\" width=\"40\" height=\"40\" x=\"-20\" y=\"-20\" style=\"overflow: visible;\">\n" +
            "            <svg id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 32 32\">\n" +
            "                <style>\n" +
            "                    .st0 {\n" +
            "                        fill: #0066ff;\n" +
            "                    }\n" +
            "                </style>\n" +
            "                <g id=\"XMLID_2_\">\n" +
            "                    <path id=\"XMLID_4_\" class=\"st0\" d=\"M16 0C7.2 0 0 7.2 0 16s7.2 16 16 16 16-7.2 16-16S24.8 0 16 0zm0 30C8.3 30 2 23.7 2 16S8.3 2 16 2s14 6.3 14 14-6.3 14-14 14z\"></path>\n" +
            "                    <path id=\"XMLID_3_\" class=\"st0\" d=\"M17 15.6V6c0-.6-.4-1-1-1s-1 .4-1 1v10c0 .1 0 .3.1.4.1.1.1.2.2.3l6 6c.2.2.5.3.7.3s.5-.1.7-.3c.4-.4.4-1 0-1.4L17 15.6z\"></path>\n" +
            "                </g>\n" +
            "            </svg>\n" +
            "        </svg>\n" +
            "    </g>\n" +
            "\t  <text id=\"lpm\" font-family='\"Courier\"' font-size=\"12\" font-weight=\"bold\" fill=\"#1a1a1a\" x=\"20\" y=\"250\">10-10-1998 to 10-10-1998</text>\n" +
            "\n" +
            "</svg>\n";
}
