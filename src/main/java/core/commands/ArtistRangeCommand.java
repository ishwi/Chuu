//package core.commands;
//
//import dao.ChuuService;
//import dao.entities.TimeFrameEnum;
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Pattern;
//
//public class ArtistRangeCommand extends ConcurrentCommand<core.parsers.params.CommandParameters> {
//    public ArtistRangeCommand(ChuuService dao) {
//        super(dao);
//    }
//
//    @Override
//    protected CommandCategory initCategory() {
//        return CommandCategory.BOT_STATS;
//    }
//
//    @Override
//    public core.parsers.Parser<core.parsers.params.CommandParameters> initParser() {
//        return new core.parsers.NoOpParser();
//    }
//
//    @Override
//    public String getDescription() {
//        return "d";
//    }
//
//    @Override
//    public List<String> getAliases() {
//        return List.of("nam");
//    }
//
//    @Override
//    public String getName() {
//        return "n";
//    }
//
//    @Override
//    public void onMessageReceived(MessageReceivedEvent e) {
//        class b extends ConcurrentCommand<core.parsers.params.CommandParameters> {
//            public b(ChuuService dao) {
//                super(dao);
//            }@Override protected CommandCategory initCategory() {
//                return CommandCategory.BOT_STATS;
//            }
//
//            @Override public core.parsers.Parser<core.parsers.params.CommandParameters> initParser() {
//                return new core.parsers.NoOpParser();
//            }
//
//            @Override public String getDescription() {
//                return "d";
//            }
//
//            @Override public List<String> getAliases() {
//                return List.of("nam");
//            }
//
//            @Override public String getName() {
//                return "n";
//            }
//
//            @Override public void onMessageReceived(MessageReceivedEvent e) {
//                try {
//                    var c = Pattern.compile("(\\w+)[\\s+]from:(.*)");
//                    var s = parser.getSubMessage(e.getMessage());
//                    var j = String.join(" ", s);
//                    var m = c.matcher(j);
//                    if (!m.matches()) {
//                        sendMessageQueue(e, "1");
//                        return;
//                    }
//                    var a = m.group(1);
//                    var p = m.group(2);
//                    var ax = new core.parsers.ChartParserAux(new String[]{p});
//                    core.parsers.utils.CustomTimeFrame t;
//                    t = ax.parseCustomTimeFrame(TimeFrameEnum.ALL);
//                    var l = getService().findLastFMData(e.getAuthor().getIdLong());
//                    var q = new core.apis.last.queues.DiscardableQueue<>(x -> !x.getArtistName().equalsIgnoreCase(a), x -> x, 1);
//                    var te = core.apis.last.TopEntity.ARTIST;
//                    lastFM.getChart(l.getName(), t, 1000, 1, te, core.apis.last.chartentities.ChartUtil.getParser(t, te, core.parsers.params.ChartParameters.toListParams(), lastFM, l.getName()), q);
//                    var o = new ArrayList<core.apis.last.chartentities.UrlCapsule>();
//                    q.drainTo(o);
//                    if (o.isEmpty()) {
//                        sendMessageQueue(e, ax + " was not found on your top 1k artists" + t.getDisplayString() + ".");
//                        return;
//                    }
//                    var u = CommandUtil.getUserInfoNotStripped(e, e.getAuthor().getIdLong()).getUsername();
//                    sendMessageQueue(e, String.format("%s has %d plays of %s%s", u, o.get(0).getPlays(), o.get(0).getArtistName(), t.getDisplayString()));
//                } catch (Exception ex) {
//                    sendMessageQueue(e, "2");
//                    return;
//                }
//            }
//
//            @Override void onCommand(MessageReceivedEvent e, core.parsers.params.CommandParameters params) throws core.exceptions.LastFmException, dao.exceptions.InstanceNotFoundException {
//
//            }
//        }
//        b b = new b(getService());
//        ctx.jda.addEventListener(new WeezerCommand(ctx.db));
//
//    }
//
//    @Override
//    void onCommand(MessageReceivedEvent e, core.parsers.params.CommandParameters params) throws core.exceptions.LastFmException, dao.exceptions.InstanceNotFoundException {
//
//    }
//}
