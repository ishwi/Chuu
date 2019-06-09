package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.AlbumInfo;
import DAO.Entities.ArtistAlbums;
import main.Exceptions.LastFmException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BandInfoCommand extends WhoKnowsCommand {

	public BandInfoCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	void whoKnowsLogic(String who, Boolean isImage, MessageReceivedEvent e) {
		ArtistAlbums ai;

		try {
			ai = lastFM.getAlbumsFromArtist(who, 10);
		} catch (LastFmException ex) {
			ex.printStackTrace();
			return;
		}


		String artist = ai.getArtist();
		List<AlbumInfo> list = ai.getAlbumList();
		String lastFmName = null;


		try {
			lastFmName = getDao().findShow(e.getAuthor().getIdLong()).getName();
		} catch (InstanceNotFoundException ex) {
			sendMessage(e, "Error Maricon");
		}


		final String username = lastFmName;
		list.parallelStream().forEach(albumInfo -> {
			try {
				albumInfo.setPlays(lastFM.getPlaysAlbum_Artist(username, artist, albumInfo.getAlbum()));

			} catch (LastFmException ex) {
				ex.printStackTrace();
			}
		});
		StringBuilder s = new StringBuilder();
		int counter = 0;
		list.sort(Comparator.comparing(AlbumInfo::getPlays).reversed());
		for (AlbumInfo albumInfo : list) {
			s.append(++counter).append(albumInfo.getAlbum()).append(" ").append(albumInfo.getPlays()).append("\n");
		}

		getDao().whoKnows(artist, e.getGuild().getIdLong());

		sendMessage(e, s.toString());
		return;


	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!band");
	}

	@Override
	public String getDescription() {
		return "Band info";
	}

	@Override
	public String getName() {
		return "Band";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!band artist\n\t --image for Image format\n\n"
		);
	}

}
