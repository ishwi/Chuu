package core.commands;

import core.exceptions.LastFmException;
import core.imagerenderer.BarChartMaker;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WeeklyCommand extends ConcurrentCommand<ChuuDataParams> {

    public WeeklyCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(getService(), new OptionalEntity("image", "displays it as a bar chart"));
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public String getDescription() {
        return "Weekly Description";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("week", "weekly");
    }

    @Override
    public String getName() {
        return "Weekly";
    }

    @Override
    void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {


        String lastFmName = params.getLastFMData().getName();
        long discordID = params.getLastFMData().getDiscordId();
        boolean isBarChart = params.hasOptional("image");

        Map<Track, Integer> durationsFromWeek = lastFM.getTrackDurations(lastFmName, TimeFrameEnum.WEEK);

        Instant instant = Instant.now();
        ZoneId zoneId = getService().getUserTimezone(discordID).toZoneId();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
        ZonedDateTime zdtStart = zdt.toLocalDate().atStartOfDay(zoneId);
        ZonedDateTime zdtPrevious7Days = zdtStart.plusDays(-7);
        Instant from = Instant.from(zdtPrevious7Days);
        Instant to = Instant.from(zdtStart);
        MultiValuedMap<LocalDateTime, Map.Entry<Integer, Integer>> map = new HashSetValuedHashMap<>();

        List<TimestampWrapper<Track>> tracksAndTimestamps = lastFM
                .getTracksAndTimestamps(lastFmName, (int) from.getEpochSecond(), (int) to.getEpochSecond());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, d/M:", Locale.UK);
        SecondsTimeFrameCount minutesWastedOnMusicDaily = new SecondsTimeFrameCount(TimeFrameEnum.WEEK);
        AtomicInteger totalSeconds = new AtomicInteger(0);
        AtomicInteger totalTracks = new AtomicInteger();
        for (TimestampWrapper<Track> tracksAndTimestamp : tracksAndTimestamps) {
            Integer seconds;
            if ((seconds = durationsFromWeek.get(tracksAndTimestamp.getWrapped())) == null) {
                seconds = 200;
            }
            Instant trackInstant = Instant.ofEpochSecond(tracksAndTimestamp.getTimestamp());
            ZonedDateTime tempZdt = ZonedDateTime.ofInstant(trackInstant, zoneId);
            tempZdt = tempZdt.toLocalDate().atStartOfDay(zoneId);
            map.put(tempZdt.toLocalDateTime(), Map.entry(tracksAndTimestamp.getTimestamp(), seconds));
        }
        StringBuilder s = new StringBuilder();
        Map<LocalDate, Integer> imageMap = new HashMap<>();
        map.asMap().entrySet().stream().sorted((x, y) -> {
                    LocalDateTime key = x.getKey();
                    LocalDateTime key1 = y.getKey();
                    return key.compareTo(key1);
                }
        ).forEach(x -> {
            LocalDateTime time = x.getKey();
            Collection<Map.Entry<Integer, Integer>> value = x.getValue();
            int seconds = value.stream().mapToInt(Map.Entry::getValue).sum();
            if (isBarChart) {
                imageMap.put(time.toLocalDate(), x.getValue().size());
            } else {
                totalSeconds.addAndGet(seconds);
                totalTracks.addAndGet(x.getValue().size());
                minutesWastedOnMusicDaily.setSeconds(seconds);
                minutesWastedOnMusicDaily.setCount(value.size());

                s.append("**")
                        .append(time.format(dtf))
                        .append("** ")
                        .append(minutesWastedOnMusicDaily.getMinutes()).append(" minutes, ")
                        .append(String
                                .format("(%d:%02dh)", minutesWastedOnMusicDaily.getHours(),
                                        minutesWastedOnMusicDaily.getRemainingMinutes()))

                        .append(" on ").append(minutesWastedOnMusicDaily.getCount())
                        .append(" tracks\n");
            }
        });


        if (isBarChart) {
            BufferedImage bufferedImage = BarChartMaker.makeBarChart(imageMap);
            sendImage(bufferedImage, e);
        } else {
            DiscordUserDisplay userInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordID);
            String url = userInfo.getUrlImage();
            String usableName = userInfo.getUsername();
            minutesWastedOnMusicDaily.setSeconds(totalSeconds.get());
            EmbedBuilder embedBuilder = new EmbedBuilder().setDescription(s)
                    .setColor(CommandUtil.randomColor())
                    .setTitle(usableName + "'s week", CommandUtil.getLastFmUser(lastFmName))
                    .setThumbnail(url)
                    .setFooter(String.format("%s has listen to %d distinct tracks (%d total tracks)%n for a total of %s", CommandUtil.markdownLessUserString(usableName, discordID, e), durationsFromWeek.size(), totalTracks.get(),
                            String.format("%d %s and %02d %s ", minutesWastedOnMusicDaily.getHours(), CommandUtil.singlePlural(minutesWastedOnMusicDaily.getHours(), "hour", "hours"),
                                    minutesWastedOnMusicDaily.getRemainingMinutes(), CommandUtil.singlePlural(minutesWastedOnMusicDaily.getMinutes(), "minute", "minutes"))));
            MessageBuilder mes = new MessageBuilder();
            e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue();
        }
    }
}
