package core.commands;

import dao.DaoImplementation;
import dao.entities.NowPlayingArtist;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.NpParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

abstract class NpCommand extends ConcurrentCommand {


	NpCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new NpParser(getDao());

	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

		String[] returned = parser.parse(e);
		String username = returned[0];
		//long discordId = Long.parseLong(returned[0]);

		NowPlayingArtist nowPlayingArtist = lastFM.getNowPlayingInfo(username);
			doSomethingWithArtist(nowPlayingArtist, e);


	}

	protected abstract void doSomethingWithArtist(NowPlayingArtist artist, MessageReceivedEvent e);
}
