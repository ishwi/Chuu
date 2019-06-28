package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.APIs.last.ConcurrentLastFM;
import main.Exceptions.LastFmException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;

public class ArtistAlbumParser extends DaoParser {
	private final ConcurrentLastFM lastFM;

	public ArtistAlbumParser(DaoImplementation dao, ConcurrentLastFM lastFM) {
		super(dao);
		this.lastFM = lastFM;
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		String messageRaw = e.getMessage().getContentRaw();
		StringBuilder builder = new StringBuilder();
		List<Member> members = e.getMessage().getMentionedMembers();
		Member sample;
		String artist;
		String album;

		if (!members.isEmpty()) {
			if (members.size() != 1) {
				sendError("Only one user pls", e);
				return null;
			}
			sample = members.get(0);
			messageRaw = messageRaw.replace(sample.getAsMention(), "");
		} else
			sample = e.getMember();


		String[] subMessage = getSubMessage(messageRaw);
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

			artist = np.getArtistName();
			album = np.getAlbumName();
		} else {

			for (String s : subMessage) {
				builder.append(s).append(" ");
			}
			String s = builder.toString();
			String[] content = s.split("\\s*-\\s*");

			if (content.length != 2) {
				sendError(this.getErrorMessage(1), e);
				return null;
			}

			artist = content[0].trim();
			album = content[1].trim();
		}
		return new String[]{artist, album, sample.getNickname()};
	}

	@Override
	public void setUpErrorMessages() {
		errorMessages.put(1, "You need to use - to separate artist and album!");
		errorMessages.put(2, "Internal Server Error, try again later");
		errorMessages.put(3, "Didn't find what you were looking for");
	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + " *artist-album*** \n\n");

	}
}
