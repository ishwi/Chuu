package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.DateParser;
import core.parsers.Parser;
import core.parsers.params.DateParameters;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.utils.TimeFormat;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScrobblesSinceCommand extends ConcurrentCommand<DateParameters> {

    public ScrobblesSinceCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<DateParameters> initParser() {
        return new DateParser(db);
    }

    @Override
    public String getDescription() {
        return "The number of scrobbles from a really flexible date";
    }

    @Override
    public List<String> getAliases() {
        return List.of("since", "scrobbles", "s");
    }

    @Override
    public String getName() {
        return "Scrobble count";
    }

    @Override
    protected void onCommand(Context e, @NotNull DateParameters params) throws LastFmException, InstanceNotFoundException {

        LastFMData lastFMData = db.findLastFMData(params.getUser().getIdLong());
        ZonedDateTime date = params.getDate().atZoneSameInstant(lastFMData.getTimeZone().toZoneId());
        int i = lastFM.scrobblesSince(lastFMData, date.toOffsetDateTime());
        String username = CommandUtil.getUserInfoConsideringGuildOrNot(e, params.getUser().getIdLong()).getUsername();
        String mmmmD = date.format(DateTimeFormatter.ofPattern("MMMM d"));

        String ending = params.isAllTime() ? "" : "since " + CommandUtil.getDateTimestampt(params.getDate().toInstant(), TimeFormat.DATE_TIME_SHORT);
        sendMessageQueue(e, String.format("%s has a total of %d scrobbles %s", username, i, ending));
    }

}
