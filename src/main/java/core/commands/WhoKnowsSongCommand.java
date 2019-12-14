package core.commands;

import dao.DaoImplementation;
import dao.entities.AlbumUserPlays;
import dao.entities.ReturnNowPlaying;
import dao.entities.Track;
import dao.entities.UsersWrapper;
import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WhoKnowsSongCommand extends WhoKnowsAlbumCommand {
	private final DiscogsApi discogsApi;
	private final Spotify spotify;

	public WhoKnowsSongCommand(DaoImplementation dao) {
		super(dao);
		this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
		this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
		this.parser = new ArtistSongParser(dao, lastFM);
	}

	@Override
	Map<UsersWrapper, Integer> fillPlayCounter(List<UsersWrapper> userList, String artist, String track, AlbumUserPlays fillWithUrl) {
		Map<UsersWrapper, Integer> userMapPlays = new LinkedHashMap<>();
		userList.forEach(u -> {
			try {
				Track trackInfo = lastFM.getTrackInfo(u.getLastFMName(), artist, track);
				userMapPlays.put(u, trackInfo.getPlays());
				if (fillWithUrl.getAlbum_url().isEmpty() && trackInfo.getImageUrl() != null)
					fillWithUrl.setAlbum_url(trackInfo.getImageUrl());

				if (fillWithUrl.getAlbum().isEmpty())
					fillWithUrl.setAlbum(trackInfo.getName());
			} catch (LastFmException ex) {
				Chuu.getLogger().warn(ex.getMessage(), ex);
			}
		});
		if (fillWithUrl.getAlbum_url().isEmpty()) {
			fillWithUrl.setAlbum_url(CommandUtil.getArtistImageUrl(getDao(), artist, lastFM, discogsApi, spotify));
		}
		return userMapPlays;
	}

	@Override
	void doExtraThings(List<ReturnNowPlaying> list2, long id, String artist, String album) {
	}

	@Override
	public String getDescription() {
		return "Get the list of people that have played a specific song on this server";
	}

	@Override
	public String getName() {
		return "Who Knows Song";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("wktrack", "whoknowstrack", "wkt");
	}
}
