package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import dao.ChuuService;
import dao.entities.SecondsTimeFrameCount;
import dao.entities.TimeFrameEnum;
import dao.entities.TimestampWrapper;
import dao.entities.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WeeklyCommand extends ConcurrentCommand {

    public WeeklyCommand(ChuuService dao) {
        super(dao);
        parser = new OnlyUsernameParser(dao);
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

    @SuppressWarnings("unchecked")
    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned = parser.parse(e);
        String lastFmName = returned[0];
        long discordID = Long.parseLong(returned[1]);

        Map<Track, Integer> durationsFromWeek = lastFM.getDurationsFromWeek(lastFmName);

        Instant instant = Instant.now();
        ZoneId zoneId = ZoneOffset.UTC;
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
        map.asMap().entrySet().stream().sorted((x, y) -> {
                    LocalDateTime key = x.getKey();
                    LocalDateTime key1 = y.getKey();
                    return key.compareTo(key1);
                }
        ).forEach(x -> {
            LocalDateTime time = x.getKey();
            Collection<Map.Entry<Integer, Integer>> value = x.getValue();

            int seconds = value.stream().mapToInt(Map.Entry::getValue).sum();

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
        });
        StringBuilder url = new StringBuilder();
        StringBuilder usableName = new StringBuilder();

        CommandUtil.getUserInfoConsideringGuildOrNot(usableName, url, e, discordID);

        EmbedBuilder embedBuilder = new EmbedBuilder().setDescription(s)
                .setColor(CommandUtil.randomColor())
                .setTitle(usableName.toString() + "'s week", CommandUtil.getLastFmUser(lastFmName))
                .setThumbnail(url.toString().isEmpty() ? null : url.toString());

        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue();

    }
}
