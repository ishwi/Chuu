package core.commands.abstracts;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.NpParser;
import core.parsers.Parser;
import core.parsers.params.NowPlayingParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.ScrobbledArtist;

import javax.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;

public abstract class NpCommand extends ConcurrentCommand<NowPlayingParameters> {


    protected NpCommand(ChuuService dao) {
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
    protected void onCommand(Context e, @NotNull NowPlayingParameters params) {
        NowPlayingArtist np = params.getNowPlayingArtist();
        doSomethingWithArtist(np, e, params.getLastFMData().getDiscordId(), params.getLastFMData(), params);
        CompletableFuture.runAsync(() -> {
            if (np.url() != null && !np.url().isBlank()) {
                try {
                    ScrobbledArtist scrobbledArtist = CommandUtil.onlyCorrection(db, np.artistName(), lastFM, true);
                    long trackValidate = CommandUtil.trackValidate(db, scrobbledArtist, lastFM, np.songName());
                    db.updateTrackImage(trackValidate, np.url());
                } catch (LastFmException instanceNotFoundException) {
                    Chuu.getLogger().warn(instanceNotFoundException.getMessage(), instanceNotFoundException);
                        }
                    }
                }
        );

    }

    protected abstract void doSomethingWithArtist(NowPlayingArtist artist, Context e, long discordId, LastFMData user, NowPlayingParameters parameters);
}
