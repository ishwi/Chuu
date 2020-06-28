package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.HotMaker;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class BillboardCommand extends ConcurrentCommand<NumberParameters<CommandParameters>> {

    private final Spotify spotify;
    private final DiscogsApi discogsApi;
    private final static ConcurrentSkipListSet<Long> inProcessSets = new ConcurrentSkipListSet<>();
    private final static ConcurrentSkipListSet<Long> usersBeeingProcessed = new ConcurrentSkipListSet<>();


    public BillboardCommand(ChuuService dao) {

        super(dao);
        spotify = SpotifySingleton.getInstance();
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        respondInPrivate = false;

    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> getParser() {

        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be between 1 and 100");
        String s = "You can also introduce a number to vary the number of tracks shown in the image" +
                "defaults to 5";
        NumberParser<CommandParameters, NoOpParser> extraParser = new NumberParser<>(new NoOpParser(),
                5L,
                100L,
                map, s, false, true, false);

        extraParser.addOptional(new OptionalEntity("--scrobbles", "sort the top by scrobble count, not listeners"));
        extraParser.addOptional(new OptionalEntity("--full", "in case of doing the image show first 100 songs in the image"));
        extraParser.addOptional(new OptionalEntity("--list", "display it in an embed"));
        return extraParser;
    }

    @Override
    public String getDescription() {
        return "The most popular tracks last week on this server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("billboard", "trend");
    }

    @Override
    public String getName() {
        return "Server's Billboard Top 100";
    }

    // You have to call the insert_weeks procedure first that is declared in MariadBnew. on the mysql client it would be something like `call inert_weeks()`

    public List<BillboardEntity> getEntities(int weekId, long guildId, boolean doListeners) {
        return getService().getBillboard(weekId, guildId, doListeners);

    }

    public String getTitle() {
        return "";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

        NumberParameters<CommandParameters> params = parser.parse(e);
        long guildId = e.getGuild().getIdLong();
        List<UsersWrapper> all = getService().getAll(guildId);
        if (all.isEmpty()) {
            sendMessageQueue(e, "There is not a single person registered in this server");
            return;
        }
        Week week = getService().getCurrentWeekId();
        int weekId = week.getId();
        boolean doListeners = !params.hasOptional("--scrobbles");
        List<BillboardEntity> entities = getEntities(weekId, guildId, doListeners);
        Date weekStart = week.getWeekStart();
        LocalDateTime weekBeggining = weekStart.toLocalDate().minus(1, ChronoUnit.WEEKS).atStartOfDay();
        if (entities.isEmpty() && weekId == 1 && this instanceof BillboardAlbumCommand && !getService().getBillboard(weekId, guildId, doListeners).isEmpty()) {
            sendMessageQueue(e, "The album trend couldn't be computed this week because it was the first one.");
            return;
        }
        if (entities.isEmpty()) {
            synchronized (inProcessSets) {
                if (inProcessSets.contains(guildId)) {
                    sendMessageQueue(e, "This weekly chart is still being calculated, wait a few seconds/minutes more pls.");
                    return;
                }
                inProcessSets.add(guildId);
            }
            try {
                Thread.sleep(10000L);
                sendMessageQueue(e, "Didn't have the top from this week, will start to make it now.");
                Map<String, List<TrackWithArtistId>> toValidate = new HashMap<>();
                all.parallelStream().forEach(x -> {
                    if (!usersBeeingProcessed.contains(x.getDiscordID()))
                        if (getService().getUserData(weekId, x.getLastFMName()).isEmpty()) {
                            try {
                                usersBeeingProcessed.add(x.getDiscordID());
                                List<TrackWithArtistId> tracksAndTimestamps = lastFM.getWeeklyBillboard(x.getLastFMName(),
                                        (int) weekBeggining.toEpochSecond(OffsetDateTime.now().getOffset())
                                        , (int) weekStart.toLocalDate().atStartOfDay().toEpochSecond(OffsetDateTime.now().getOffset()));
                                List<TrackWithArtistId> value = tracksAndTimestamps.stream().collect(Collectors.groupingBy(
                                        t -> t, Collectors.counting())).entrySet().stream().map(u -> {
                                    TrackWithArtistId key = u.getKey();
                                    key.setPlays(Math.toIntExact(u.getValue()));
                                    return key;
                                }).collect(Collectors.toList());
                                toValidate.put(x.getLastFMName(), value);
                            } catch (LastFmException ignored) {

                            } finally {
                                usersBeeingProcessed.remove(x.getDiscordID());
                            }
                        }
                });

                Map<String, Set<TrackWithArtistId>> indexMap = toValidate.values().stream().flatMap(Collection::stream).collect(Collectors.groupingBy(Track::getArtist, Collectors.toSet()));

                List<ScrobbledArtist> artists = indexMap.keySet().stream().map(x -> new ScrobbledArtist(x, 0, null)).collect(Collectors.toList());
                getService().filldArtistIds(artists);
                Map<Boolean, List<ScrobbledArtist>> collect1 = artists.stream().collect(Collectors.partitioningBy(x -> x.getArtistId() != -1L && x.getArtistId() != 0));
                List<ScrobbledArtist> foundArtists = collect1.get(true);
                Map<String, String> changedUserNames = new HashMap<>();
                collect1.get(false).stream().map(x -> {
                    try {
                        String artist = x.getArtist();
                        CommandUtil.validate(getService(), x, lastFM, discogsApi, spotify);
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


                for (Map.Entry<String, List<TrackWithArtistId>> tbInserted : toValidate.entrySet()) {
                    String lastfmId = tbInserted.getKey();
                    List<TrackWithArtistId> data = tbInserted.getValue();
                    data = data.stream().peek(x -> {
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
                    }).filter(x -> x.getArtistId() != -1L).collect(Collectors.toList());
                    getService().insertUserData(weekId, lastfmId, data);
                }
                getService().insertBillboardData(weekId, guildId);


                entities = getEntities(weekId, guildId, doListeners);


                sendMessageQueue(e, "Successfully Generated these week's charts");
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            } finally {
                inProcessSets.remove(guildId);
            }
        }
        if (entities.isEmpty()) {
            sendMessageQueue(e, "Didn't found any scrobble in this server users");
            return;
        }
        String name = e.getGuild().getName();
        doBillboard(e, params, doListeners, entities, weekStart.toLocalDate().atStartOfDay(), weekBeggining, name);
    }

    protected void doBillboard(MessageReceivedEvent e, NumberParameters<CommandParameters> params, boolean doListeners, List<BillboardEntity> entities, LocalDateTime weekStart, LocalDateTime weekBeggining, String name) {
        if (params.hasOptional("--list")) {

            EmbedBuilder embedBuilder = new EmbedBuilder();
            List<String> artistAliases = entities
                    .stream().map(x -> String.format(". **[%s](%s):**\n Rank: %d | Previous Week: %s | Peak: %s | Weeks on top: %s | %s: %d\n",
                            x.getArtist() == null ? CommandUtil.cleanMarkdownCharacter(x.getName()) : CommandUtil.cleanMarkdownCharacter(x.getName() + " - " + x.getArtist()),
                            x.getArtist() == null ? CommandUtil.getLastFmArtistUrl(x.getName()) : CommandUtil.getLastFMArtistTrack(x.getArtist(), x.getName()),
                            x.getPosition(),
                            x.getPreviousWeek() == 0 ? "--" : x.getPreviousWeek(),
                            x.getPeak() == 0 ? "--" : x.getPeak(),
                            x.getStreak() == 0 ? "--" : x.getStreak(),
                            doListeners ? "Listeners" : "Scrobbles",
                            x.getListeners()

                    )).collect(Collectors.toList());
            StringBuilder a = new StringBuilder();
            for (int i = 0; i < 10 && i < artistAliases.size(); i++) {
                a.append(i + 1).append(artistAliases.get(i));
            }

            embedBuilder.setTitle("Billboard Top 100 " + getTitle() + "from " + name)
                    .setColor(CommandUtil.randomColor())
                    .setDescription(a);
            MessageBuilder mes = new MessageBuilder();
            e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                    new Reactionary<>(artistAliases, message1, embedBuilder));
        } else {
            BufferedImage logo = CommandUtil.getLogo(getService(), e);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM");
            String one = formatter.format(weekBeggining.toLocalDate());
            String dayOne = weekBeggining.getDayOfMonth() + CommandUtil.getDayNumberSuffix(weekBeggining.getDayOfMonth());
            String second = formatter.format(weekStart.toLocalDate());
            String daySecond = weekStart.toLocalDate().getDayOfMonth() + CommandUtil.getDayNumberSuffix(weekStart.toLocalDate().getDayOfMonth());


            int size = params.hasOptional("--full") ? 100 : Math.toIntExact(params.getExtraParam());
            sendImage(HotMaker.doHotMaker(name + "'s " + getTitle() + "chart", dayOne + " " + one + " -  " + daySecond + " " + second, entities, doListeners, size, logo), e);
        }
    }

    @Override
    public String getUsageInstructions() {
        return super.getUsageInstructions() + "The chart gets filled with the top 1k tracks of each user from the previous week's Monday to this week's Monday. Once the chart is built if new users come to the server they wont affect the chart. Come back next Monday to generate the next chart";
    }
}
