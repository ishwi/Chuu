package core.commands.billboard;

import core.apis.last.entities.chartentities.*;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.CollageMaker;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.HotMaker;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import core.parsers.utils.OptionalEntity;
import core.parsers.utils.Optionals;
import core.services.BillboardHoarder;
import core.util.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class BillboardCommand extends ConcurrentCommand<NumberParameters<CommandParameters>> {

    private final static ConcurrentSkipListSet<Long> inProcessSets = new ConcurrentSkipListSet<>();


    public BillboardCommand(ServiceView dao) {

        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.TRENDS;
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {

        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be between 1 and 100");
        String s = "You can also introduce a number to vary the number of tracks shown in the image" +
                "defaults to 5";
        NumberParser<CommandParameters, NoOpParser> extraParser = new NumberParser<>(NoOpParser.INSTANCE,
                5L,
                100L,
                map, s, false, true, false, "count");

        extraParser.addOptional(new OptionalEntity("scrobbles", "sort the top by scrobble count, not listeners"),
                new OptionalEntity("full", "in case of doing the image show first 100 songs in the image"),
                Optionals.LIST.opt,
                Optionals.IMAGE.opt,
                Optionals.NOTITLES.opt,
                Optionals.ASIDE.opt,
                Optionals.PLAYS_REPLACE.opt);
        extraParser.replaceOptional("plays", Optionals.NOPLAYS.opt);
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

    public List<BillboardEntity> getEntities(int weekId, long guildId, boolean doListeners, Context event) {
        return db.getBillboard(weekId, guildId, doListeners);

    }

    public String getTitle() {
        return "";
    }

    @Override
    public void onCommand(Context e, @Nonnull NumberParameters<CommandParameters> params) {


        long guildId = e.getGuild().getIdLong();
        List<UsersWrapper> all = db.getAllNonPrivate(guildId);
        if (all.isEmpty()) {
            sendMessageQueue(e, "There is not a single person registered in this server");
            return;
        }
        Week week = db.getCurrentWeekId();
        Date weekStart = week.getWeekStart();
        Optional<UsersWrapper> min = all.stream().min(Comparator.comparingInt(x -> x.getTimeZone().getOffset(Instant.now().getEpochSecond())));

        if (min.isPresent()) {
            UsersWrapper usersWrapper = min.get();
            TimeZone timeZone = usersWrapper.getTimeZone();
            if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.MONDAY)) {
                int offset = timeZone.getOffset(LocalDate.now().atStartOfDay().toInstant(
                        ZoneOffset.UTC).getEpochSecond() * 1000);
                if (offset > 0) {
                    ZonedDateTime plus = LocalDate.now().atStartOfDay().atZone(ZoneId.of("UTC")).plus(offset, ChronoUnit.MILLIS);
                    if (plus.isAfter(ZonedDateTime.now())) {
                        long remaining = offset - LocalTime.now().toNanoOfDay() / 1_000_000;
                        if (remaining > 0) {
                            String format = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(remaining),
                                    TimeUnit.MILLISECONDS.toMinutes(remaining) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(remaining)),
                                    TimeUnit.MILLISECONDS.toSeconds(remaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remaining)));
                            sendMessageQueue(e, "The week hasn't ended for a user because they have set a different timezone!" + "\nYou will have to wait " + format);
                            return;
                        }
                    }
                }
            }
        }
        int weekId = week.getId();
        boolean doListeners = !params.hasOptional("scrobbles");
        List<BillboardEntity> entities = getEntities(weekId, guildId, doListeners, e);
        LocalDateTime weekBeggining = weekStart.toLocalDate().minus(1, ChronoUnit.WEEKS).atStartOfDay();

        if (entities.isEmpty() && weekId == 1 && this instanceof BillboardAlbumCommand && !db.getBillboard(weekId, guildId, doListeners).isEmpty()) {
            sendMessageQueue(e, "The album trend couldn't be computed this week because it was the first one.");
            return;
        }
        if (entities.isEmpty()) {

            if (inProcessSets.contains(guildId)) {
                sendMessageQueue(e, "This weekly chart is still being calculated, wait a few seconds/minutes more pls.");
                return;
            }
            try {
                inProcessSets.add(guildId);
                sendMessageQueue(e, "Didn't have the top from this week, will start to make it now.");
                BillboardHoarder billboardHoarder = new BillboardHoarder(all, db, week, lastFM);
                billboardHoarder.hoardUsers();
                db.insertBillboardData(weekId, guildId);
            } finally {
                inProcessSets.remove(guildId);
            }
            entities = getEntities(weekId, guildId, doListeners, e);
            if (entities.isEmpty()) {
                sendMessageQueue(e, "Didn't find any scrobbles in this server users");
                return;
            }
            sendMessageQueue(e, "Successfully generated this week's charts");
        }

        String name = e.getGuild().getName();
        doBillboard(e, params, doListeners, entities, weekStart.toLocalDate().atStartOfDay(), weekBeggining, name, true);
    }


    protected void doBillboard(Context e, NumberParameters<CommandParameters> params, boolean doListeners, List<BillboardEntity> entities, LocalDateTime weekStart, LocalDateTime weekBeggining, String name, boolean isFromGuild) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM");
        String one = formatter.format(weekBeggining.toLocalDate());
        String dayOne = weekBeggining.getDayOfMonth() + CommandUtil.getDayNumberSuffix(weekBeggining.getDayOfMonth());
        String second = formatter.format(weekStart.toLocalDate());
        String daySecond = weekStart.toLocalDate().getDayOfMonth() + CommandUtil.getDayNumberSuffix(weekStart.toLocalDate().getDayOfMonth());
        String subtitle = dayOne + " " + one + " - " + daySecond + " " + second;
        String url = isFromGuild ? e.getGuild().getIconUrl() : e.getJDA().getSelfUser().getAvatarUrl();

        if (params.hasOptional("list")) {
            EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);
            List<String> artistAliases = entities
                    .stream().map(x -> String.format(". **[%s](%s):**\n Rank: %d | Previous Week: %s | Peak: %s | Weeks on top: %s | %s: %d%n%n",
                            x.getArtist() == null ? CommandUtil.escapeMarkdown(x.getName()) : CommandUtil.escapeMarkdown(x.getName() + " - " + x.getArtist()),
                            x.getArtist() == null ? LinkUtils.getLastFmArtistUrl(x.getName()) : LinkUtils.getLastFMArtistTrack(x.getArtist(), x.getName()),
                            x.getPosition(),
                            x.getPreviousWeek() == 0 ? "--" : x.getPreviousWeek(),
                            x.getPeak() == 0 ? "--" : x.getPeak(),
                            x.getStreak() == 0 ? "--" : x.getStreak(),
                            doListeners ? "Listeners" : "Scrobbles",
                            x.getListeners()

                    )).toList();

            embedBuilder.setAuthor("Top 100 " + getTitle() + "from " + name + " in " + subtitle, null, url);

            new PaginatorBuilder<>(e, embedBuilder, artistAliases).build().queue();

        } else if (params.hasOptional("image")) {
            AtomicInteger ranker = new AtomicInteger(0);
            int size = entities.size();
            int x = Math.max((int) Math.ceil(Math.sqrt(size)), 1);
            int y = (int) Math.ceil((double) size / x);
            if (y == 1) {
                x = size;
            }
            boolean drawTitles = !params.hasOptional("notitles");
            boolean drawPlays = !params.hasOptional("noplays");
            boolean isAside = params.hasOptional("aside");
            try {
                LastFMData data = e.isFromGuild() ? db.computeLastFmData(e.getAuthor().getIdLong(), e.getGuild().getIdLong()) : db.findLastFMData(e.getAuthor().getIdLong());
                isAside = isAside || EnumSet.of(ChartMode.IMAGE_ASIDE, ChartMode.IMAGE_ASIDE_INFO).contains(data.getChartMode());
            } catch (InstanceNotFoundException ex) {
                // Shallowed
            }


            boolean finalIsAside = isAside;
            List<UrlCapsule> urlEntities = entities.stream()
                    .limit(size)
                    .map(w -> {
                        if (w.getName() == null) {
                            if (doListeners) {
                                return new ArtistListenersChart(w.getUrl(), ranker.getAndIncrement(), w.getArtist(), null, Math.toIntExact(w.getListeners()), drawTitles, drawPlays, finalIsAside);
                            } else {
                                return new ArtistChart(w.getUrl(), ranker.getAndIncrement(), w.getArtist(), null, Math.toIntExact(w.getListeners()), drawTitles, drawPlays, finalIsAside);
                            }
                        } else {
                            if (doListeners) {
                                return new AlbumListenersChart(w.getUrl(), ranker.getAndIncrement(), w.getName(), w.getArtist(), null, Math.toIntExact(w.getListeners()), drawTitles, drawPlays, finalIsAside);
                            } else {
                                return new AlbumChart(w.getUrl(), ranker.getAndIncrement(), w.getName(), w.getArtist(), null, Math.toIntExact(w.getListeners()), drawTitles, drawPlays, finalIsAside);
                            }
                        }
                    })
                    .toList();

            ChartQuality quality = GraphicUtils.getQuality(urlEntities.size(), e);
            BufferedImage image = CollageMaker.generateCollageThreaded(x, y, new ArrayBlockingQueue<>(urlEntities.size(), false, urlEntities), quality,
                    isAside);
            sendImage(image, e, quality, new ChuuEmbedBuilder(e).setAuthor("Top 100 " + getTitle() + "from " + name + " in " + subtitle, null, url));
        } else {
            BufferedImage logo = CommandUtil.getLogo(db, e);


            int size = params.hasOptional("full") ? 100 : Math.toIntExact(params.getExtraParam());
            sendImage(HotMaker.doHotMaker(name + "'s " + getTitle() + "chart", subtitle, entities, doListeners, size, logo), e);
        }
    }

    @Override
    public String getUsageInstructions() {
        return super.getUsageInstructions() + "The chart gets filled with the top 1k tracks of each user from the previous week's Monday to this week's Monday. Once the chart is built if new users come to the server they wont affect the chart. Come back next Monday to generate the next chart";
    }
}
