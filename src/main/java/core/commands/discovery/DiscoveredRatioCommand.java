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
import dao.entities.ScrobbledArtist;
import dao.entities.TimeFrameEnum;

import javax.annotation.Nonnull;
import java.util.List;

public class DiscoveredRatioCommand extends ConcurrentCommand<TimeFrameParameters> {
    public DiscoveredRatioCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public Parser<TimeFrameParameters> initParser() {
        return new TimerFrameParser(db, TimeFrameEnum.WEEK);
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
    public String slashName() {
        return "artist-ratio";
    }

    @Override
    public String getName() {
        return "Discovery Ratio";
    }

    @Override
    protected void onCommand(Context e, @Nonnull TimeFrameParameters params) throws LastFmException {


        if (params.getTime() == TimeFrameEnum.ALL) {
            sendMessageQueue(e, "Surprisingly you have discovered a 100% of your artist");
            return;
        }
        String name = params.getLastFMData().getName();
        List<ScrobbledArtist> allArtists = lastFM.getAllArtists(params.getLastFMData(), CustomTimeFrame.ofTimeFrameEnum(params.getTime()));
        int size = db.getDiscoveredArtists(allArtists, name).size();
        String userString = getUserString(e, params.getLastFMData().getDiscordId());
        sendMessageQueue(e, String.format("%s has discovered **%s** new %s%s, making that **%s%%** of new artists discovered.", userString, size, CommandUtil.singlePlural(size, "artist", "artists"), params.getTime().getDisplayString(), ScoredAlbumRatings.formatter.format(size * 100. / allArtists.size())));

    }
}
