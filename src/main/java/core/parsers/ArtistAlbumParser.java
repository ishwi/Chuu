package core.parsers;

import dao.DaoImplementation;
import dao.entities.NowPlayingArtist;
import core.apis.last.ConcurrentLastFM;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class ArtistAlbumParser extends DaoParser {
	final ConcurrentLastFM lastFM;

	public ArtistAlbumParser(DaoImplementation dao, ConcurrentLastFM lastFM) {
		super(dao);
		this.lastFM = lastFM;
	}


	@Override
	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException, LastFmException {
		User sample;

		if (e.isFromGuild()) {
			List<Member> members = e.getMessage().getMentionedMembers();
			if (!members.isEmpty()) {
				if (members.size() != 1) {
					sendError("Only one user pls", e);
					return null;
				}
				sample = members.get(0).getUser();
				subMessage = Arrays.stream(subMessage).filter(s -> !s.equals(sample.getAsMention()))
						.toArray(String[]::new);
			} else {
				sample = e.getMember().getUser();
			}
		} else
			sample = e.getAuthor();

		if (subMessage.length == 0) {

			NowPlayingArtist np;

			String userName = dao.findLastFMData(sample.getIdLong()).getName();
			np = lastFM.getNowPlayingInfo(userName);

			return doSomethingWithNp(np, sample, e);

		} else {
			return doSomethingWithString(subMessage, sample, e);
		}
	}


	String[] doSomethingWithNp(NowPlayingArtist np, User ignored, MessageReceivedEvent e) {
		return new String[]{np.getArtistName(), np.getAlbumName(), String.valueOf(e.getAuthor().getIdLong())};
	}

	String[] doSomethingWithString(String[] subMessage, User sample, MessageReceivedEvent e) {
		StringBuilder builder = new StringBuilder();
		for (String s : subMessage) {
			builder.append(s).append(" ");
		}
		String s = builder.toString();
		String regex = "(?<!\\\\)" + ("\\s*-\\s*");
		String[] content = s.split(regex);

		//String[] content = s.split("\\s*-\\s*");

		if (content.length < 2) {
			sendError(this.getErrorMessage(5), e);
			return null;
		}
		if (content.length > 2) {
			sendError(this.getErrorMessage(7), e);
			return null;
		}

		String artist = content[0].trim().replaceAll("\\\\-", "-");
		String album = content[1].trim().replaceAll("\\\\-", "-");

		return new String[]{artist, album, String.valueOf(sample.getIdLong())};
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *artist-album** username* " +
				"\n\tif username its not specified";

	}

	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();
		errorMessages.put(5, "You need to use - to separate artist and album!");
		errorMessages.put(6, "Didn't find what you were looking for");
		errorMessages
				.put(7, "You need to add the escape character **\"\\\\\"** in the **\"-\"** that appear on the album or artist.\n " +
						"\tFor example: Artist - Alb**\\\\-**um  ");

	}
}
