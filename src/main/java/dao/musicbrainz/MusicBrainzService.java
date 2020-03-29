package dao.musicbrainz;

import com.neovisionaries.i18n.CountryCode;
import dao.entities.*;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public interface MusicBrainzService {


    List<AlbumInfo> listOfYearReleases(List<AlbumInfo> mbiz, Year year);

    List<AlbumInfo> listOfCurrentYear(List<AlbumInfo> mbiz);

    List<AlbumInfo> findArtistByRelease(List<AlbumInfo> releaseInfo, Year year);

    List<AlbumInfo> findArtistByReleaseLowerCase(List<AlbumInfo> releaseInfo, Year year);


    List<AlbumInfo> findArtistByReleaseCurrentYear(List<AlbumInfo> releaseInfo);

    Map<Genre, Integer> genreCount(List<AlbumInfo> releaseInfo);

    List<Track> getAlbumTrackList(String artist, String album);

    List<Track> getAlbumTrackListLowerCase(String artist, String album);

    Map<Country, Integer> countryCount(List<ArtistInfo> artistInfo);

    List<ArtistUserPlays> getArtistFromCountry(CountryCode country, BlockingQueue<UrlCapsule> allUserArtist, long discordId);

    List<Track> getAlbumTrackListMbid(String mbid);

    ArtistMusicBrainzDetails getArtistDetails(ArtistInfo artist);

    List<TrackInfo> getAlbumInfoByNames(List<UrlCapsule> urlCapsules);

    void getAlbumInfoByMbid(List<UrlCapsule> urlCapsules);

}

