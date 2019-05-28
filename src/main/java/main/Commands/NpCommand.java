package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.APIs.Parsers.NpParser;
import main.APIs.Parsers.Parser;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Exceptions.ParseException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class NpCommand extends ConcurrentCommand {


	public NpCommand(DaoImplementation dao) {
		super(dao);
	}

	public abstract void doSomethingWithArtist(NowPlayingArtist artist, Parser parser, MessageReceivedEvent e);

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		String[] message = getSubMessage(e.getMessage());
		return new String[]{getLastFmUsername1input(message, e.getAuthor().getIdLong(), e)};

	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request:\n";
		String message;
		if (code == 0) {
			userNotOnDB(e, code);
			return;
		}

		if (code == 2) {
			message = "User hasnt played any song recently!";

		} else {
			message = "There was a problem with Last FM Api " + cause;
		}
		sendMessage(e, base + message);
	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {

		Parser parser = new NpParser(e, getDao());
		String[] returned = parser.parse();
		if (returned == null) {
			return;
		}
		String username = returned[0];
		try {
			NowPlayingArtist nowPlayingArtist = lastFM.getNowPlayingInfo(username);
			doSomethingWithArtist(nowPlayingArtist, parser, e);


		} catch (
				LastFMNoPlaysException e1) {
			parser.sendError(parser.getErrorMessage(3));
		} catch (LastFmEntityNotFoundException e2) {
			parser.sendError(parser.getErrorMessage(4));
		} catch (
				LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2));
		}

	}
}
