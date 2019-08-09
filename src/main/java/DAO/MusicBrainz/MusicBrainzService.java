package DAO.MusicBrainz;

import DAO.Entities.*;

import java.time.Year;
import java.util.List;
import java.util.Map;

public interface MusicBrainzService {


	List<AlbumInfo> listOfYearReleases(List<AlbumInfo> mbiz, Year year);

	List<AlbumInfo> listOfCurrentYear(List<AlbumInfo> mbiz);

	List<AlbumInfo> findArtistByRelease(List<AlbumInfo> releaseInfo, Year year);

	List<AlbumInfo> findArtistByReleaseCurrentYear(List<AlbumInfo> releaseInfo);

	Map<Genre, Integer> genreCount(List<AlbumInfo> releaseInfo);

	List<Track> getAlbumTrackList(String artist, String album);

	Map<Country, Integer> countryCount(List<ArtistInfo> artistInfo);
}
