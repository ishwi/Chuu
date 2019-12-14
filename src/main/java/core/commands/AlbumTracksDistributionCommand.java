package core.commands;

import com.google.common.collect.Multimaps;
import dao.DaoImplementation;
import dao.entities.ArtistData;
import dao.entities.FullAlbumEntity;
import dao.entities.LastFMData;
import dao.entities.Track;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.TrackDistributor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

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
	public String getDescription() {
		return "Plays on each track of the provided album";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("tracks", "tt");
	}

	@Override
	public String getName() {
		return "Track Distribution";
	}

	@Override
	void doSomethingWithAlbumArtist(String artist, String album, MessageReceivedEvent e, long who) throws InstanceNotFoundException, LastFmException {

		FullAlbumEntity fullAlbumEntity;
		String artistUrl;
		LastFMData data = getDao().findLastFMData(who);

		ArtistData artistData = new ArtistData("", artist, 0);
		CommandUtil.lessHeavyValidate(getDao(), artistData, lastFM, discogsApi, spotify);
		artist = artistData.getArtist();
		artistUrl = artistData.getUrl();

		fullAlbumEntity = lastFM.getTracksAlbum(data.getName(), artist, album);

		List<Track> trackList = fullAlbumEntity.getTrackList();
		if (trackList.isEmpty()) {

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

			if (trackList.isEmpty()) {
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

				if (trackList.isEmpty()) {
					//If is still empty well fuck it

					sendMessageQueue(e, "Couldn't find a tracklist for " + fullAlbumEntity
							.getArtist() + " - " + fullAlbumEntity
							.getAlbum());
					return;
				}

			}

		}
		List<Track> handler = new ArrayList<>(trackList);

		List<Track> collect = Multimaps.index(handler, Track::getPosition)
				.asMap().values().stream()
				.map(value -> {
					Optional<Track> max = value.stream().max(Comparator.comparingInt(Track::getPlays));
					return max.orElse(null);
				}).filter(Objects::nonNull).sorted(Comparator.comparingInt(Track::getPosition))
				.collect(Collectors.toList());
		if (trackList.stream().mapToInt(Track::getPlays).sum() <= collect.stream().mapToInt(Track::getPlays).sum()) {
			fullAlbumEntity.setTrackList(collect);
		}

		fullAlbumEntity.setArtistUrl(artistUrl);
		BufferedImage bufferedImage = TrackDistributor.drawImage(fullAlbumEntity, false);
		sendImage(bufferedImage, e);
	}
}
