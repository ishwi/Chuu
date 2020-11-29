package core.commands;

import core.Chuu;
import core.exceptions.LastFmException;
import core.parsers.NpParser;
import core.parsers.Parser;
import core.parsers.params.NowPlayingParameters;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;

abstract class NpCommand extends ConcurrentCommand<NowPlayingParameters> {


    NpCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.NOW_PLAYING;
    }

    @Override
    public Parser<NowPlayingParameters> initParser() {
        return new NpParser(getService(), lastFM);
    }

    @Override
    public void onCommand(MessageReceivedEvent e, @NotNull NowPlayingParameters params) throws LastFmException, InstanceNotFoundException {
        doSomethingWithArtist(params.getNowPlayingArtist(), e, params.getLastFMData().getDiscordId());
        CompletableFuture.runAsync(() -> {
                    if (params.getNowPlayingArtist().getUrl() != null && !params.getNowPlayingArtist().getUrl().isBlank()) {
                        try {
                            ScrobbledArtist scrobbledArtist = CommandUtil.onlyCorrection(getService(), params.getNowPlayingArtist().getArtistName(), lastFM, true);
                            long trackValidate = CommandUtil.trackValidate(getService(), scrobbledArtist, lastFM, params.getNowPlayingArtist().getSongName());
                            getService().updateTrackImage(trackValidate, params.getNowPlayingArtist().getUrl());
                        } catch (LastFmException instanceNotFoundException) {
                            Chuu.getLogger().warn(instanceNotFoundException.getMessage(), instanceNotFoundException);
                        }
                    }
                }
        );

    }

    protected abstract void doSomethingWithArtist(NowPlayingArtist artist, MessageReceivedEvent e, long discordId);
}
