package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.AlbumUserPlays;
import DAO.Entities.Track;
import DAO.Entities.UsersWrapper;
import main.APIs.Discogs.DiscogsApi;
import main.APIs.Discogs.DiscogsSingleton;
import main.APIs.Spotify.Spotify;
import main.APIs.Spotify.SpotifySingleton;
import main.Chuu;
import main.Exceptions.LastFmException;
import main.Parsers.ArtistSongParser;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WhoKnowsSongCommand extends WhoKnowsAlbum {
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
				if (fillWithUrl.getAlbum_url().isEmpty()) {
					fillWithUrl.setAlbum_url(trackInfo.getImageUrl());
				}
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
	public String getDescription() {
		return "Get the list of people that have played a specific song on this server";
	}

	@Override
	public String getName() {
		return "Who Knows Song";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("!wktrack", "!whoknowstrack", "!wkt");
	}
}
