package dao.musicbrainz;

import com.neovisionaries.i18n.CountryCode;
import dao.entities.*;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public interface MusicBrainzService {


    List<AlbumInfo> listOfYearReleases(List<AlbumInfo> mbiz, Year year);

    List<AlbumInfo> listOfYearRangeReleases(List<AlbumInfo> mbiz, int baseYear, int numberOfYears);


    List<CountWrapper<AlbumInfo>> listOfYearReleasesWithAverage(List<AlbumInfo> mbiz, Year year);

    List<CountWrapper<AlbumInfo>> listOfRangeYearReleasesWithAverage(List<AlbumInfo> mbiz, int baseYear, int numberOfYears);

    List<AlbumInfo> listOfCurrentYear(List<AlbumInfo> mbiz);

    List<AlbumInfo> findArtistByRelease(List<AlbumInfo> releaseInfo, Year year);

    List<AlbumInfo> findArtistByReleaseRangeYear(List<AlbumInfo> releaseInfo, int baseYear, int numberOfYears);


    List<AlbumInfo> findArtistByReleaseLowerCase(List<AlbumInfo> releaseInfo, Year year);


    List<AlbumInfo> findArtistByReleaseCurrentYear(List<AlbumInfo> releaseInfo);

    Map<Genre, Integer> genreCount(List<AlbumInfo> releaseInfo);

    Map<Genre, Integer> genreCountByartist(List<ArtistInfo> releaseInfo);

    List<Track> getAlbumTrackList(String artist, String album);

    List<Track> getAlbumTrackListLowerCase(String artist, String album);

    Set<String> albumsGenre(List<AlbumInfo> releaseInfo, String genre);

    List<AlbumInfo> albumsGenreByName(List<AlbumInfo> releaseInfo, String genre);

    Map<Country, Integer> countryCount(List<ArtistInfo> artistInfo);

    List<ArtistUserPlays> getArtistFromCountry(CountryCode country, BlockingQueue<UrlCapsule> allUserArtist, long discordId);

    List<Track> getAlbumTrackListMbid(String mbid);

    ArtistMusicBrainzDetails getArtistDetails(ArtistInfo artist);

    List<TrackInfo> getAlbumInfoByNames(List<UrlCapsule> urlCapsules);

    void getAlbumInfoByMbid(List<UrlCapsule> urlCapsules);


    MusicbrainzFullAlbumEntity getAlbumInfo(FullAlbumEntityExtended albumInfo);

    List<CountWrapper<AlbumInfo>> findArtistByReleaseLowerCaseWithAverage(List<AlbumInfo> emptyMbid, Year year);

    List<CountWrapper<AlbumInfo>> findArtistByReleaseWithAverage(List<AlbumInfo> emptyMbid, Year year);

    List<CountWrapper<AlbumInfo>> findArtistByReleaseWithAverageRangeYears(List<AlbumInfo> emptyMbid, int baseYear, int numberOfYears);

    Map<Language, Long> getLanguageCountByMbid(List<AlbumInfo> withMbid);

    List<AlbumGenre> getAlbumRecommendationsByGenre(Map<Genre, Integer> map, List<ScrobbledArtist> recs);

}

