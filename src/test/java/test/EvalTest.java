package core.commands;

import core.commands.utils.EvalContext;
import dao.ChuuService;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class EvalTest {
    public void name(EvalContext ctx) {
        class b extends ConcurrentCommand<core.parsers.params.CommandParameters> {
            public b(ChuuService dao) {
                super(dao);
            }

            @Override
            protected CommandCategory initCategory() {
                return CommandCategory.BOT_STATS;
            }

            @Override
            public core.parsers.Parser<core.parsers.params.CommandParameters> initParser() {
                return new core.parsers.NoOpParser();
            }

            @Override
            public String getDescription() {
                return "d";
            }

            @Override
            public List<String> getAliases() {
                return List.of("nam");
            }

            @Override
            public String getName() {
                return "n";
            }

            @Override
            public void onMessageReceived(MessageReceivedEvent e) {
                try {
                    var c = java.util.regex.Pattern.compile("(\\w+)[\\s+]from:(.*)");
                    var s = parser.getSubMessage(e.getMessage());
                    var j = String.join(" ", s);
                    var m = c.matcher(j);
                    if (!m.matches()) {
                        sendMessageQueue(e, "1");
                        return;
                    }
                    var a = m.group(1);
                    var p = m.group(2);
                    var ax = new core.parsers.ChartParserAux(new String[]{p});
                    core.parsers.utils.CustomTimeFrame t;
                    t = ax.parseCustomTimeFrame(TimeFrameEnum.ALL);
                    var l = getService().findLastFMData(e.getAuthor().getIdLong());
                    var q = new core.apis.last.queues.DiscardableQueue<>(x -> !x.getArtistName().equalsIgnoreCase(a), x -> x, 1);
                    var te = core.apis.last.TopEntity.ARTIST;
                    lastFM.getChart(l.getName(), t, 1000, 1, te, core.apis.last.chartentities.ChartUtil.getParser(t, te, core.parsers.params.ChartParameters.toListParams(), lastFM, l.getName()), q);
                    var o = new ArrayList<core.apis.last.chartentities.UrlCapsule>();
                    q.drainTo(o);
                    if (o.isEmpty()) {
                        sendMessageQueue(e, ax + " was not found on your top 1k artists" + t.getDisplayString() + ".");
                        return;
                    }
                    sendMessageQueue(e, String.format("You has %d plays of %s%s", o.get(0).getPlays(), o.get(0).getArtistName(), t.getDisplayString()));
                } catch (Exception ex) {
                    sendMessageQueue(e, "2");
                }
            }

            @Override
            void onCommand(MessageReceivedEvent e, core.parsers.params.CommandParameters params) {
            }
        }
        ctx.jda.addEventListener(new b(ctx.db));
    }

    public JSONObject getKey(String id) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("type", id);
        return jsonObject;

    }

    @Test
    public void a() {

        Pattern one = Pattern.compile("1[\\s]*[/\\\\][\\s]*3");
        Pattern oneEyy = Pattern.compile("O(dd)?[\\s]*?e(ye)?[\\s]*C(ircle)?", Pattern.CASE_INSENSITIVE);
        Pattern kl = Pattern.compile("kim[\\s]*lip", Pattern.CASE_INSENSITIVE);
        Pattern gw = Pattern.compile("go[\\s]*won", Pattern.CASE_INSENSITIVE);
        Pattern oh = Pattern.compile("(olivia hye)|(hye)|(olivia)", Pattern.CASE_INSENSITIVE);
        Map<String, JSONObject> jsonObjectMap = new HashMap<>();
        JSONArray jsonArray = new JSONArray();

        JSONArray objects = new JSONArray(new JSONTokener(new InputStreamReader(WhoKnowsLoonasCommand.class.getResourceAsStream("/loonas.json"), StandardCharsets.UTF_8)));
        for (int i = 0; i < objects.length(); i++) {
            JSONObject jsonObject = objects.getJSONObject(i);
            String name = jsonObject.getString("name");

            jsonObjectMap.put(name, jsonObject);
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(EvalTest.class.getResourceAsStream("/images/strings.txt"), StandardCharsets.UTF_8));
        List<String> collect = bufferedReader.lines().map(x -> x.replaceAll("\"", "")).filter(x -> !x.isBlank()).distinct().collect(Collectors.toList());
        for (String s1 : collect) {


            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", s1);
            JSONObject jsonObject1 = jsonObjectMap.get(s1);
            if (jsonObject1 != null) {
                System.out.println(jsonObject1);
                continue;
            }
            jsonObject.put("group", new JSONArray());
            String s = s1.replaceAll("[()]", "");
            if (s1.toLowerCase().contains("yyxy")) {
                jsonObject.accumulate("group", getKey(Loonas.YYXY.id));
            } else if (s1.length() < 7) {
                jsonObject.accumulate("group", getKey(Loonas.MAIN.id));
            } else if (one.matcher(s1).find()) {
                jsonObject.accumulate("group", getKey(Loonas.ONETHIRD.id));
            } else if (s.toLowerCase().endsWith("loona") || s.toLowerCase().endsWith("looΠΔ".toLowerCase()) || s.toLowerCase().endsWith("소녀".toLowerCase())) {
                jsonObject.accumulate("group", getKey(Loonas.MAIN.id));
            } else if (oneEyy.matcher(s1).find()) {
                jsonObject.accumulate("group", getKey(Loonas.OEC.id));
            } else {
                if (kl.matcher(s1).find()) {
                    jsonObject.accumulate("group", getKey(Loonas.KIM_LIP.id));
                }
                if (gw.matcher(s1).find()) {
                    jsonObject.accumulate("group", getKey(Loonas.GOWON.id));
                }
                if (oh.matcher(s1).find()) {
                    jsonObject.accumulate("group", getKey(Loonas.OLIVIA.id));
                }
                EnumSet<Loonas> a = EnumSet.complementOf(EnumSet.of(Loonas.OLIVIA, Loonas.GOWON, Loonas.KIM_LIP));
                for (Loonas loonas : a) {
                    if (s1.toUpperCase().contains(loonas.id)) {
                        jsonObject.accumulate("group", getKey(loonas.id));
                    }
                }
                if (jsonObject.optJSONArray("group") == null || jsonObject.optJSONArray("group").length() == 0) {
                    if (s1.toLowerCase().contains("오드아이써클".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.OEC.id));
                    }
                    if (s1.toLowerCase().contains("희진".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.HEEJIN.id));
                    }
                    if (s1.toLowerCase().contains("하슬".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.HASEUL.id));
                    }
                    if (s1.toLowerCase().contains("츄".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.CHUU.id));
                    }
                    if (s1.toLowerCase().contains("최리".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.CHOERRY.id));
                    }
                    if (s1.toLowerCase().contains("진솔".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.JINSOUL.id));
                    }
                    if (s1.toLowerCase().contains("이브".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.YVES.id));
                    }
                    if (s1.toLowerCase().contains("여진".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.YEOJIN.id));
                    }
                    if (s1.toLowerCase().contains("김립".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.KIM_LIP.id));
                    }
                    if (s1.toLowerCase().contains("고원".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.GOWON.id));
                    }
                    if (s1.toLowerCase().contains("비비".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.VIVI.id));
                    }
                    if (s1.toLowerCase().contains("올리비아".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.OLIVIA.id));
                    }
                    if (s1.toLowerCase().contains("현진".toLowerCase())) {
                        jsonObject.accumulate("group", getKey(Loonas.HYUNJIN.id));
                    }
                    if (jsonObject.getJSONArray("group").length() == 0) {

                        jsonObject.accumulate("group", getKey(Loonas.MISC.id));
                    }
                }
                objects.put(jsonObject);
            }

        }

        String s = objects.toString();
        System.out.println(s);
    }

    private enum TYPE {
        GROUP, SUBUNIT, PERSON, MISC
    }

    private enum Loonas {
        MAIN("MAIN", TYPE.GROUP), YYXY("YYXY", TYPE.SUBUNIT), ONETHIRD("1/3", TYPE.SUBUNIT), OEC("ODD EYE CIRCLE", TYPE.SUBUNIT),
        CHUU("CHUU", TYPE.PERSON), YVES("YVES", TYPE.PERSON),
        GOWON("GOWON", TYPE.PERSON), HEEJIN("HEEJIN", TYPE.PERSON), CHOERRY("CHOERRY", TYPE.PERSON),
        HASEUL("HASEUL", TYPE.PERSON), OLIVIA("OLIVIA", TYPE.PERSON), HYUNJIN("HYUNJIN", TYPE.PERSON),
        JINSOUL("JINSOUL", TYPE.PERSON), VIVI("VIVI", TYPE.PERSON), KIM_LIP("KIM LIP", TYPE.PERSON),
        YEOJIN("YEOJIN", TYPE.PERSON), MISC("MISC", TYPE.MISC);

        private final String id;
        private final TYPE type;

        Loonas(String id, TYPE group) {
            this.id = id;
            this.type = group;
        }
    }
}

