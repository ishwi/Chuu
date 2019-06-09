package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Parsers.ArtistAlbumParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;

// usage !command "song" "/@user"
//Right now only for author
public class AlbumSongPlaysCommand extends ConcurrentCommand {
	public AlbumSongPlaysCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new ArtistAlbumParser();
	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] parsed;
		parsed = parser.parse(e);
		boolean needNp = Boolean.parseBoolean(parsed[0]);
		String artist;
		String album;
		String userName = null;

		try {
			if (needNp) {

				try {
					userName = getDao().findShow(e.getAuthor().getIdLong()).getName();
				} catch (InstanceNotFoundException ex) {
					sendMessage(e, "You need to introduce artist-album  or to be registered on the bot!");
					return;
				}
				NowPlayingArtist np = lastFM.getNowPlayingInfo(userName);
				artist = np.getArtistName();
				album = np.getAlbumName();
			} else {
				artist = parsed[1];
				album = parsed[2];
			}

			int a = lastFM.getPlaysAlbum_Artist(getDao().findShow(e.getAuthor().getIdLong()).getName(), artist, album);
			Member b = e.getGuild().getMemberById(e.getAuthor().getIdLong());
			if (b != null)
				userName = b.getEffectiveName();
			sendMessage(e, "**" + userName + "** has listened " + a + " times the album **" + album + "** by **" + artist + "**!");
		} catch (LastFmEntityNotFoundException | InstanceNotFoundException e1) {
			parser.sendError(parser.getErrorMessage(3), e);
		} catch (LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
		}
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!album");
	}

	@Override
	public String getDescription() {
		return ("How many times you have heard an album!");
	}

	@Override
	public String getName() {
		return "Get Plays Album";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList
				("!album artist-album \n\n");

	}
}
