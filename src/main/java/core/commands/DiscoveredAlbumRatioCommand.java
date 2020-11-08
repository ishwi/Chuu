package core.commands;

import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.TimeFrameParameters;
import dao.ChuuService;
import dao.entities.ScoredAlbumRatings;
import dao.entities.ScrobbledAlbum;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;


public class DiscoveredAlbumRatioCommand extends ConcurrentCommand<TimeFrameParameters> {
    public DiscoveredAlbumRatioCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<TimeFrameParameters> initParser() {
        return new TimerFrameParser(getService(), TimeFrameEnum.WEEK);
    }

    @Override
    public String getDescription() {
        return "Returns the ratio of new albums discovered in the provided timeframe";
    }

    @Override
    public List<String> getAliases() {
        return List.of("albumdiscoveryratio", "albdratio", "aldiscoveryratio", "aldisratio");
    }

    @Override
    public String getName() {
        return "Album Discovery Ratio";
    }

    @Override
    void onCommand(MessageReceivedEvent e, @NotNull TimeFrameParameters params) throws LastFmException, InstanceNotFoundException {


        if (params.getTime().equals(TimeFrameEnum.ALL)) {
            sendMessageQueue(e, "Surprisingly you have discovered a 100% of your albums");
            return;
        }
        List<ScrobbledAlbum> allArtists = lastFM.getAllAlbums(params.getLastFMData().getName(), params.getTime().toApiFormat());
        int size = getService().getDiscoveredAlbums(allArtists, params.getLastFMData().getName()).size();
        String userString = getUserString(e, params.getLastFMData().getDiscordId());
        sendMessageQueue(e, String.format("%s has discovered **%s** new %s%s, making that **%s%%** of new albums discovered.", userString, size, CommandUtil.singlePlural(size, "album", "albums"), params.getTime().getDisplayString(), ScoredAlbumRatings.formatter.format(size * 100. / allArtists.size())));

    }
}
