package core.commands.stats;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.DateParser;
import core.parsers.Parser;
import core.parsers.params.DateParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScrobblesSinceCommand extends ConcurrentCommand<DateParameters> {

    public ScrobblesSinceCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<DateParameters> initParser() {
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
    protected void onCommand(MessageReceivedEvent e, @NotNull DateParameters params) throws LastFmException, InstanceNotFoundException {

        LastFMData lastFMData = getService().findLastFMData(params.getUser().getIdLong());
        ZonedDateTime date = params.getDate().atZoneSameInstant(lastFMData.getTimeZone().toZoneId());
        int i = lastFM.scrobblesSince(lastFMData, date.toOffsetDateTime());
        String username = CommandUtil.getUserInfoConsideringGuildOrNot(e, params.getUser().getIdLong()).getUsername();
        String mmmmD = date.format(DateTimeFormatter.ofPattern("MMMM d"));
        sendMessageQueue(e, String.format("%s has a total of %d scrobbles since %s%s %d %s", username, i, mmmmD, CommandUtil.getDayNumberSuffix(date.getDayOfMonth()),
                date.getYear(), date.getMinute() == 0 && date.getHour() == 0 && date.getSecond() == 0
                        ? "" : date.format(DateTimeFormatter.ofPattern("HH:mm x"))));
    }

}
