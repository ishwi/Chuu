package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.TimeFrameParameters;
import dao.ChuuService;
import dao.entities.ScoredAlbumRatings;
import dao.entities.ScrobbledArtist;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class DiscoveredRatioCommand extends ConcurrentCommand<TimeFrameParameters> {
    public DiscoveredRatioCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<TimeFrameParameters> getParser() {
        return new TimerFrameParser(getService(), TimeFrameEnum.WEEK);
    }

    @Override
    public String getDescription() {
        return "Returns the ratio of new artist discovered in the provided timeframe";
    }

    @Override
    public List<String> getAliases() {
        return List.of("discoveryratio", "dratio");
    }

    @Override
    public String getName() {
        return "Discovery Ratio";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        TimeFrameParameters parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        if (parse.getTime().equals(TimeFrameEnum.ALL)) {
            sendMessageQueue(e, "Surprisingly you have discovered a 100% of your artist");
            return;
        }
        List<ScrobbledArtist> allArtists = lastFM.getAllArtists(parse.getLastFMData().getName(), parse.getTime().toApiFormat());
        int size = getService().getDiscoveredArtists(allArtists, parse.getLastFMData().getName()).size();
        sendMessageQueue(e, String.format("You have discovered **%s** new artists%s, making it a **%s%%** of new artist discovered.", size, parse.getTime().getDisplayString(), ScoredAlbumRatings.formatter.format(size * 100. / allArtists.size())));

    }
}
