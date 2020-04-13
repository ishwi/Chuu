package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.DateParser;
import core.parsers.Parser;
import core.parsers.params.DateParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScrobblesSinceCommand extends ConcurrentCommand<DateParameters> {

    public ScrobblesSinceCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public Parser<DateParameters> getParser() {
        return new DateParser(getService());
    }

    @Override
    public String getDescription() {
        return "The number of scrobbles from a really flexible date";
    }

    @Override
    public List<String> getAliases() {
        return List.of("since");
    }

    @Override
    public String getName() {
        return "Since";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        DateParameters parameters = parser.parse(e);
        if (parameters == null) {
            return;
        }
        LastFMData lastFMData = getService().findLastFMData(parameters.getUser().getIdLong());
        OffsetDateTime date = parameters.getDate();
        int i = lastFM.scrobblesSince(lastFMData.getName(), date);
        String username = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getUser().getIdLong()).getUsername();
        String mmmmD = date.format(DateTimeFormatter.ofPattern("MMMM d"));
        sendMessageQueue(e, String.format("%s has a total of %d scrobbles since %s%s %d %s", username, i, mmmmD, CommandUtil.getDayNumberSuffix(date.getDayOfMonth()),
                date.getYear(), date.getMinute() == 0 && date.getHour() == 0 && date.getSecond() == 0
                        ? "" : date.format(DateTimeFormatter.ofPattern("HH:mm x"))));
    }

}
