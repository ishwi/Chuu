package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.FullAlbumEntity;
import DAO.Entities.LastFMData;
import DAO.Entities.Track;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AlbumTracksDistributionCommand extends AlbumSongPlaysCommand {

	public AlbumTracksDistributionCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	void doSomethingWithAlbumArtist(String artist, String album, MessageReceivedEvent e, long who) {

		try {
			LastFMData data = getDao().findLastFMData(who);

			FullAlbumEntity fullAlbumEntity = lastFM.getTracksAlbum(data.getName(), artist, album);

			Member b = e.getGuild().getMemberById(who);
			String usernameString = data.getName();
			if (b != null)
				usernameString = b.getEffectiveName();

			StringBuilder s = new StringBuilder();
			s.append(fullAlbumEntity.getArtist()).append(" - ").append(fullAlbumEntity.getAlbum()).append("\n")
					.append("Total plays: ").append(fullAlbumEntity.getTotalPlayNumber()).append("\n");
			int counter = 1;
			List<Track> trackList = fullAlbumEntity.getTrackList();
			trackList.sort(Comparator.comparingInt(Track::getPlays).reversed());
			for (Track track : trackList) {
				s.append(counter++).append(". Track #").append(track.getPosition()).append(": ").append(track.getName())
						.append(" ")
						.append(track.getPlays())
						.append(" duration = ").append(track.getDuration()).append("\n");
			}

			sendMessage(e, "**" + usernameString + "**\n " + s);

		} catch (InstanceNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(5), e);
		} catch (LastFmEntityNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(6), e);
		} catch (LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
		}
	}

	@Override
	public String getDescription() {
		return "Plays on each track of the provided album";
	}

	@Override
	public String getName() {
		return "Track Distribution";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!tracks");
	}
}
