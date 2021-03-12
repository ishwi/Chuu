package core.commands.billboard;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.HotMaker;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import core.services.BillboardHoarder;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.BillboardEntity;
import dao.entities.UsersWrapper;
import dao.entities.Week;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class BillboardCommand extends ConcurrentCommand<NumberParameters<CommandParameters>> {

    private final static ConcurrentSkipListSet<Long> inProcessSets = new ConcurrentSkipListSet<>();


    public BillboardCommand(ChuuService dao) {

        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {

        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be between 1 and 100");
        String s = "You can also introduce a number to vary the number of tracks shown in the image" +
                "defaults to 5";
        NumberParser<CommandParameters, NoOpParser> extraParser = new NumberParser<>(new NoOpParser(),
                5L,
                100L,
                map, s, false, true, false);

        extraParser.addOptional(new OptionalEntity("scrobbles", "sort the top by scrobble count, not listeners"));
        extraParser.addOptional(new OptionalEntity("full", "in case of doing the image show first 100 songs in the image"));
        extraParser.addOptional(new OptionalEntity("list", "display it in an embed"));
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

    public List<BillboardEntity> getEntities(int weekId, long guildId, boolean doListeners, MessageReceivedEvent event) {
        return db.getBillboard(weekId, guildId, doListeners);

    }

    public String getTitle() {
        return "";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<CommandParameters> params) {


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
        doBillboard(e, params, doListeners, entities, weekStart.toLocalDate().atStartOfDay(), weekBeggining, name);
    }


    protected void doBillboard(MessageReceivedEvent e, NumberParameters<CommandParameters> params, boolean doListeners, List<BillboardEntity> entities, LocalDateTime weekStart, LocalDateTime weekBeggining, String name) {
        if (params.hasOptional("list")) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            List<String> artistAliases = entities
                    .stream().map(x -> String.format(". **[%s](%s):**\n Rank: %d | Previous Week: %s | Peak: %s | Weeks on top: %s | %s: %d\n",
                            x.getArtist() == null ? CommandUtil.cleanMarkdownCharacter(x.getName()) : CommandUtil.cleanMarkdownCharacter(x.getName() + " - " + x.getArtist()),
                            x.getArtist() == null ? LinkUtils.getLastFmArtistUrl(x.getName()) : LinkUtils.getLastFMArtistTrack(x.getArtist(), x.getName()),
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
                    .setColor(ColorService.computeColor(e))
                    .setDescription(a);
            e.getChannel().sendMessage(embedBuilder.build()).queue(message1 ->
                    new Reactionary<>(artistAliases, message1, embedBuilder));
        } else {
            BufferedImage logo = CommandUtil.getLogo(db, e);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM");
            String one = formatter.format(weekBeggining.toLocalDate());
            String dayOne = weekBeggining.getDayOfMonth() + CommandUtil.getDayNumberSuffix(weekBeggining.getDayOfMonth());
            String second = formatter.format(weekStart.toLocalDate());
            String daySecond = weekStart.toLocalDate().getDayOfMonth() + CommandUtil.getDayNumberSuffix(weekStart.toLocalDate().getDayOfMonth());


            int size = params.hasOptional("full") ? 100 : Math.toIntExact(params.getExtraParam());
            sendImage(HotMaker.doHotMaker(name + "'s " + getTitle() + "chart", dayOne + " " + one + " - " + daySecond + " " + second, entities, doListeners, size, logo), e);
        }
    }

    @Override
    public String getUsageInstructions() {
        return super.getUsageInstructions() + "The chart gets filled with the top 1k tracks of each user from the previous week's Monday to this week's Monday. Once the chart is built if new users come to the server they wont affect the chart. Come back next Monday to generate the next chart";
    }
}
