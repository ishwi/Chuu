package main.APIs.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.APIs.last.ConcurrentLastFM;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WhoKnowsNpParser extends NpParser {
	private ConcurrentLastFM lastFM;

	public WhoKnowsNpParser(MessageReceivedEvent e, DaoImplementation dao, ConcurrentLastFM last) {
		super(e, dao);
		this.lastFM = last;
	}

	@Override
	public String[] parse() {
		String[] a = super.parse();
		String[] message = getSubMessage(e.getMessage());

		String user = a[0];
		boolean hasImage = false;
		String[] optional = containsOptional("image", message);
		if (optional.length == message.length)
			hasImage = true;

		NowPlayingArtist nowPlayingArtist;
		try {
			nowPlayingArtist = lastFM.getNowPlayingInfo(user);


		} catch (LastFMNoPlaysException e1) {
			sendError(getErrorMessage(3));
			return null;

		} catch (LastFmException e1) {
			sendError(getErrorMessage(2));
			return null;
		}
		return new String[]{nowPlayingArtist.getArtistName(), Boolean.toString(hasImage)};


	}

	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();
	}
}
