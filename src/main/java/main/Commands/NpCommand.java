package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Parsers.NpParser;
import main.Parsers.Parser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class NpCommand extends ConcurrentCommand {


	NpCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new NpParser(getDao());

	}

	public abstract void doSomethingWithArtist(NowPlayingArtist artist, Parser parser, MessageReceivedEvent e);


	@Override
	public void threadableCode(MessageReceivedEvent e) {

		String[] returned = parser.parse(e);
		if (returned == null) {
			return;
		}
		String username = returned[0];
		try {
			NowPlayingArtist nowPlayingArtist = lastFM.getNowPlayingInfo(username);
			doSomethingWithArtist(nowPlayingArtist, parser, e);


		} catch (
				LastFMNoPlaysException e1) {
			parser.sendError(parser.getErrorMessage(3), e);
		} catch (LastFmEntityNotFoundException e2) {
			parser.sendError(parser.getErrorMessage(4), e);
		} catch (
				LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
		}

	}
}
