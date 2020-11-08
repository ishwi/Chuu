package core.commands;

import core.exceptions.LastFmException;
import core.parsers.NpParser;
import core.parsers.Parser;
import core.parsers.params.NowPlayingParameters;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;

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


    }

    protected abstract void doSomethingWithArtist(NowPlayingArtist artist, MessageReceivedEvent e, long discordId);
}
