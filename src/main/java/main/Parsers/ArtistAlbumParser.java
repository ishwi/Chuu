package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.APIs.last.ConcurrentLastFM;
import main.Exceptions.LastFmException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Arrays;
import java.util.List;

public class ArtistAlbumParser extends DaoParser {
	private final ConcurrentLastFM lastFM;

	public ArtistAlbumParser(DaoImplementation dao, ConcurrentLastFM lastFM) {
		super(dao);
		this.lastFM = lastFM;
	}


	@Override
	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {
		List<Member> members = e.getMessage().getMentionedMembers();
		Member sample;

		if (!members.isEmpty()) {
			if (members.size() != 1) {
				sendError("Only one user pls", e);
				return null;
			}
			sample = members.get(0);
			subMessage = Arrays.stream(subMessage).filter(s -> !s.equals(sample.getAsMention()))
					.toArray(String[]::new);
		} else
			sample = e.getMember();

		if (subMessage.length == 0) {

			NowPlayingArtist np;
			try {

				assert sample != null;
				String userName = dao.findLastFMData(sample.getUser().getIdLong()).getName();
				np = lastFM.getNowPlayingInfo(userName);

			} catch (InstanceNotFoundException ex) {
				sendError(sample.getUser().getName() + " needs to be registered on the bot!", e);
				return null;
			} catch (LastFmException ex) {
				sendError(this.getErrorMessage(2), e);
				return null;
			}

			return doSomethingWithNp(np, sample);

		} else {
			return doSomethingWithString(subMessage, sample, e);
		}
	}


	public String[] doSomethingWithNp(NowPlayingArtist np, Member sample) {
		return new String[]{np.getArtistName(), np.getAlbumName(), String.valueOf(sample.getIdLong())};
	}

	public String[] doSomethingWithString(String[] subMessage, Member sample, MessageReceivedEvent e) {
		StringBuilder builder = new StringBuilder();
		for (String s : subMessage) {
			builder.append(s).append(" ");
		}
		String s = builder.toString();
		String[] content = s.split("\\s*-\\s*");

		if (content.length != 2) {
			sendError(this.getErrorMessage(5), e);
			return null;
		}

		String artist = content[0].trim();
		String album = content[1].trim();

		return new String[]{artist, album, String.valueOf(sample.getIdLong())};
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *artist-album** username* " +
				"\n\tif username its not specified";

	}

	@Override
	public void setUpErrorMessages() {
		errorMessages.put(5, "You need to use - to separate artist and album!");
		errorMessages.put(6, "Didn't find what you were looking for");
	}
}
