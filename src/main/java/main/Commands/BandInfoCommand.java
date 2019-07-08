package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.AlbumUserPlays;
import DAO.Entities.ArtistAlbums;
import DAO.Entities.ArtistData;
import DAO.Entities.WrapperReturnNowPlaying;
import main.APIs.Discogs.DiscogsApi;
import main.APIs.Discogs.DiscogsSingleton;
import main.APIs.Spotify.Spotify;
import main.APIs.Spotify.SpotifySingleton;
import main.Exceptions.LastFmException;
import main.ImageRenderer.BandRendered;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BandInfoCommand extends WhoKnowsCommand {
	private final DiscogsApi discogsApi;
	private final Spotify spotify;

	public BandInfoCommand(DaoImplementation dao) {
		super(dao);
		this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
		this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
	}

	@Override
	void whoKnowsLogic(ArtistData who, Boolean isImage, MessageReceivedEvent e) {
		ArtistAlbums ai;



		try {
			ai = lastFM.getAlbumsFromArtist(who.getArtist(), 14);
		} catch (LastFmException ex) {
			ex.printStackTrace();
			return;
		}


		String artist = ai.getArtist();
		List<AlbumUserPlays> list = ai.getAlbumList();
		String lastFmName;


		try {
			lastFmName = getDao().findLastFMData(e.getAuthor().getIdLong()).getName();
		} catch (InstanceNotFoundException ex) {
			sendMessage(e, "Error f");
			return;
		}


		final String username = lastFmName;
		list =
				list.stream().peek(albumInfo -> {
					try {
						albumInfo.setPlays(lastFM.getPlaysAlbum_Artist(username, artist, albumInfo.getAlbum()).getPlays());

					} catch (LastFmException ex) {
						ex.printStackTrace();
					}
				})
						.filter(a -> a.getPlays() > 0)
						.collect(Collectors.toList());

		list.sort(Comparator.comparing(AlbumUserPlays::getPlays).reversed());
		ai.setAlbumList(list);
		WrapperReturnNowPlaying np = getDao().whoKnows(who.getArtist(), e.getGuild().getIdLong(), 5);
		np.getReturnNowPlayings().forEach(element ->
				element.setDiscordName(getUserString(element.getDiscordId(), e, element.getLastFMId()))
		);
		int plays = getDao().getArtistPlays(artist, username);
		BufferedImage logo = CommandUtil.getLogo(getDao(), e);
		BufferedImage returnedImage = BandRendered.makeBandImage(np, ai, plays, logo, getUserString(e.getAuthor().getIdLong(), e, username));
		sendImage(returnedImage, e);
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


}
