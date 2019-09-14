package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import DAO.Entities.FullAlbumEntity;
import DAO.Entities.LastFMData;
import DAO.Entities.Track;
import DAO.MusicBrainz.MusicBrainzService;
import DAO.MusicBrainz.MusicBrainzServiceSingleton;
import main.APIs.Discogs.DiscogsApi;
import main.APIs.Discogs.DiscogsSingleton;
import main.APIs.Spotify.Spotify;
import main.APIs.Spotify.SpotifySingleton;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.ImageRenderer.TrackDistributor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AlbumTracksDistributionCommand extends AlbumPlaysCommand {
	private final MusicBrainzService mb;
	private final DiscogsApi discogsApi;
	private final Spotify spotify;

	public AlbumTracksDistributionCommand(DaoImplementation dao) {

		super(dao);
		this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
		this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
		mb = MusicBrainzServiceSingleton.getInstance();
	}

	@Override
	void doSomethingWithAlbumArtist(String artist, String album, MessageReceivedEvent e, long who) {

		FullAlbumEntity fullAlbumEntity;
		String artistUrl;
		try {
			LastFMData data = getDao().findLastFMData(who);

			ArtistData artistData = new ArtistData("", artist, 0);
			CommandUtil.lessHeavyValidate(getDao(), artistData, lastFM, discogsApi, spotify);
			artist = artistData.getArtist();
			artistUrl = artistData.getUrl();

			fullAlbumEntity = lastFM.getTracksAlbum(data.getName(), artist, album);


		} catch (InstanceNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(1), e);
			return;
		} catch (LastFmEntityNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(6), e);
			return;
		} catch (LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
			return;
		}
		if (fullAlbumEntity.getTrackList().isEmpty()) {

			mb.getAlbumTrackList(fullAlbumEntity.getArtist(), fullAlbumEntity.getAlbum())
					.stream().map(t ->
					{
						try {
							return lastFM.getTrackInfo(fullAlbumEntity.getUsername(), t.getArtist(), t.getName());
						} catch (LastFmException ex) {
							return t;
						}
					}
			).sorted(Comparator.comparingInt(Track::getPosition)).forEach(fullAlbumEntity::addTrack);

			if (fullAlbumEntity.getTrackList().isEmpty()) {
				//Force it to lowerCase
				mb.getAlbumTrackListLowerCase(fullAlbumEntity.getArtist(), fullAlbumEntity.getAlbum())
						.stream().map(t ->
						{
							try {
								return lastFM.getTrackInfo(fullAlbumEntity.getUsername(), t.getArtist(), t.getName());
							} catch (LastFmException ex) {
								return t;
							}
						}
				).sorted(Comparator.comparingInt(Track::getPosition)).forEach(fullAlbumEntity::addTrack);

				if (fullAlbumEntity.getTrackList().isEmpty()) {

					sendMessageQueue(e, "Couldn't  find a tracklist for " + fullAlbumEntity
							.getArtist() + " - " + fullAlbumEntity
							.getAlbum());
					return;
				}

			}

			//If is still empty well fuck it
		}

		fullAlbumEntity.setArtistUrl(artistUrl);
		BufferedImage bufferedImage = TrackDistributor.drawImage(fullAlbumEntity, false);
		sendImage(bufferedImage, e);
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
		return Collections.singletonList("tracks");
	}
}
