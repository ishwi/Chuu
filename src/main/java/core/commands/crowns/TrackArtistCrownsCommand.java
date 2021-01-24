package core.commands.crowns;

import core.commands.artists.GlobalTrackArtistCrownsCommand;
import core.commands.utils.CommandCategory;
import core.parsers.params.ArtistParameters;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.TrackPlays;
import dao.entities.UniqueWrapper;

import java.util.List;

public class TrackArtistCrownsCommand extends GlobalTrackArtistCrownsCommand {

    public TrackArtistCrownsCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CROWNS;
    }


    public String getTitle(ScrobbledArtist scrobbledArtist) {
        return scrobbledArtist.getArtist() + "'s track ";
    }


    public UniqueWrapper<TrackPlays> getList(NumberParameters<ArtistParameters> params) {
        Long threshold = params.getExtraParam();
        long guildId = params.getE().getGuild().getIdLong();

        if (threshold == null) {
            threshold = (long) db.getGuildCrownThreshold(guildId);
        }
        return db.getUserArtistTrackCrowns(params.getInnerParams().getLastFMData().getName(), params.getInnerParams().getScrobbledArtist().getArtistId(), guildId, Math.toIntExact(threshold));
    }

    @Override
    public String getDescription() {
        return ("List of artist you are the top listener within a server");
    }

    @Override
    public List<String> getAliases() {
        return List.of("trackcrownsartist", "tca", "cta");
    }


    @Override
    public String getName() {
        return "Crowns track artist";
    }
}
