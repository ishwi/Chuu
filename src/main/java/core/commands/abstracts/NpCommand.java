package core.commands.abstracts;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.NpParser;
import core.parsers.Parser;
import core.parsers.params.NowPlayingParameters;
import core.services.validators.TrackValidator;
import core.util.ServiceView;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public abstract class NpCommand extends ConcurrentCommand<NowPlayingParameters> {


    protected NpCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.NOW_PLAYING;
    }

    @Override
    public Parser<NowPlayingParameters> initParser() {
        return new NpParser(db, lastFM);
    }

    @Override
    public void onCommand(Context e, @NotNull NowPlayingParameters params) {
        NowPlayingArtist np = params.getNowPlayingArtist();
        doSomethingWithArtist(np, e, params.getLastFMData().getDiscordId(), params.getLastFMData(), params);
        CommandUtil.runLog(() -> {
            if (!StringUtils.isBlank(np.url())) {
                try {
                    long trackId = new TrackValidator(db, lastFM).validate(np.artistName(), np.songName()).getTrackId();
                    db.updateTrackImage(trackId, np.url());
                } catch (LastFmException instanceNotFoundException) {
                    Chuu.getLogger().warn(instanceNotFoundException.getMessage(), instanceNotFoundException);
                }
            }
        });

    }

    protected abstract void doSomethingWithArtist(NowPlayingArtist artist, Context e, long discordId, LastFMData user, NowPlayingParameters parameters);
}
