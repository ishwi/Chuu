package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.APIs.last.ConcurrentLastFM;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

//TODO the parser is used twice and in once it doesnt do the optional --list so the usage cant be common to both
public class WhoKnowsNpParser extends NpParser {
	private final ConcurrentLastFM lastFM;

	public WhoKnowsNpParser(DaoImplementation dao, ConcurrentLastFM last) {
		super(dao);
		this.lastFM = last;
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {

		String[] message = getSubMessage(e.getMessage());

		boolean hasImage = true;
		String[] optional = containsOptional("list", message);
		if (optional.length != message.length) {
			hasImage = false;
			message = optional;
		}
		String user = getLastFmUsername1input(message, e.getAuthor().getIdLong(), e);
		if (user == null)
			return null;

		NowPlayingArtist nowPlayingArtist;
		try {
			nowPlayingArtist = lastFM.getNowPlayingInfo(user);


		} catch (LastFMNoPlaysException e1) {
			sendError(getErrorMessage(3), e);
			return null;

		} catch (LastFmException e1) {
			sendError(getErrorMessage(2), e);
			return null;
		}
		return new String[]{nowPlayingArtist.getArtistName(), Boolean.toString(hasImage)};


	}


}
