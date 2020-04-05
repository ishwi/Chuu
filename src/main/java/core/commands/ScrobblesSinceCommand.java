package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.DateParser;
import core.parsers.params.DateParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScrobblesSinceCommand extends ConcurrentCommand {

    public ScrobblesSinceCommand(ChuuService dao) {
        super(dao);
        this.parser = new DateParser(dao);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public List<String> getAliases() {
        return List.of("since");
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        DateParameters parameters = new DateParameters(parse, e);
        LastFMData lastFMData = getService().findLastFMData(parameters.getDiscordId());
        OffsetDateTime date = parameters.getDate();
        int i = lastFM.scrobblesSince(lastFMData.getName(), date);
        String username = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId()).getUsername();
        String mmmm_dd = date.format(DateTimeFormatter.ofPattern("MMMM d"));
        sendMessageQueue(e, String.format("%s has a total of %d scrobbles since %s%s %d %s", username, i, mmmm_dd, getDayNumberSuffix(date.getDayOfMonth()),
                date.getYear(), date.getMinute() == 0 && date.getHour() == 0 && date.getSecond() == 0
                        ? "" : date.format(DateTimeFormatter.ofPattern("HH:mm x"))));
    }

    private String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
}
