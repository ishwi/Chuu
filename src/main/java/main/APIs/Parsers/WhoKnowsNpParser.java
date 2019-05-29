package main.APIs.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.APIs.last.ConcurrentLastFM;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WhoKnowsNpParser extends NpParser {
	private ConcurrentLastFM lastFM;

	public WhoKnowsNpParser(DaoImplementation dao, ConcurrentLastFM last) {
		super(dao);
		this.lastFM = last;
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		String[] a = super.parse(e);
		String[] message = getSubMessage(e.getMessage());

		String user = a[0];
		boolean hasImage = true;
		String[] optional = containsOptional("list", message);
		if (optional.length != message.length)
			hasImage = false;

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

	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();
	}
}
