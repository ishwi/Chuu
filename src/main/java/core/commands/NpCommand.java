package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.NpParser;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

abstract class NpCommand extends ConcurrentCommand {


    NpCommand(ChuuService dao) {
        super(dao);
        this.parser = new NpParser(getService());

    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

        String[] returned = parser.parse(e);
        String username = returned[0];
        long discordId = Long.parseLong(returned[1]);

        NowPlayingArtist nowPlayingArtist = lastFM.getNowPlayingInfo(username);
        doSomethingWithArtist(nowPlayingArtist, e, discordId);


    }

    protected abstract void doSomethingWithArtist(NowPlayingArtist artist, MessageReceivedEvent e, long discordId);
}
