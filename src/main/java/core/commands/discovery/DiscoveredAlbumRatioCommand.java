package core.commands.discovery;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.TimeFrameParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ServiceView;
import dao.entities.ScoredAlbumRatings;
import dao.entities.ScrobbledAlbum;
import dao.entities.TimeFrameEnum;

import javax.validation.constraints.NotNull;
import java.util.List;


public class DiscoveredAlbumRatioCommand extends ConcurrentCommand<TimeFrameParameters> {
    public DiscoveredAlbumRatioCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<TimeFrameParameters> initParser() {
        return new TimerFrameParser(db, TimeFrameEnum.WEEK);
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
    protected void onCommand(Context e, @NotNull TimeFrameParameters params) throws LastFmException {


        if (params.getTime().equals(TimeFrameEnum.ALL)) {
            sendMessageQueue(e, "Surprisingly you have discovered a 100% of your albums");
            return;
        }
        List<ScrobbledAlbum> allArtists = lastFM.getAllAlbums(params.getLastFMData(), new CustomTimeFrame(params.getTime()));
        int size = db.getDiscoveredAlbums(allArtists, params.getLastFMData().getName()).size();
        String userString = getUserString(e, params.getLastFMData().getDiscordId());
        sendMessageQueue(e, String.format("%s has discovered **%s** new %s%s, making that **%s%%** of new albums discovered.", userString, size, CommandUtil.singlePlural(size, "album", "albums"), params.getTime().getDisplayString(), ScoredAlbumRatings.formatter.format(size * 100. / allArtists.size())));

    }
}
