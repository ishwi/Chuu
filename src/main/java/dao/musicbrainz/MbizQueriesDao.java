package dao.musicbrainz;

import com.neovisionaries.i18n.CountryCode;
import dao.entities.*;

import java.sql.Connection;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Set;

interface MbizQueriesDao {
    List<CountWrapper<AlbumInfo>> getYearAverage(Connection con, List<AlbumInfo> albumInfos, Year year);

    List<AlbumInfo> getYearAlbums(Connection con, List<AlbumInfo> mbizList, Year year);

    List<AlbumInfo> getYearAlbumsByReleaseName(Connection con, List<AlbumInfo> releaseInfo, Year year);

    List<AlbumInfo> getDecadeAlbumsByReleaseName(Connection con, List<AlbumInfo> releaseInfo, int decade, int numberOfYears);

    Map<Genre, Integer> genreCount(Connection connection, List<AlbumInfo> releaseInfo);

    Map<Country, Integer> countryCount(Connection connection, List<ArtistInfo> releaseInfo);

    List<Track> getAlbumTrackList(Connection connection, String artist, String album);

    List<Track> getAlbumTrackListLower(Connection connection, String artist, String album);

    List<AlbumInfo> getYearAlbumsByReleaseNameLowerCase(Connection con, List<AlbumInfo> releaseInfo, Year year);

    List<String> getArtistFromCountry(Connection connection, CountryCode country, List<ArtistInfo> allUserArtist);

    List<AlbumInfo> getAlbumsOfGenreByName(Connection con, List<AlbumInfo> releaseInfo, String genre);

    Set<String> getArtistOfGenre(Connection connection, String genre, List<AlbumInfo> releaseInfo);


    List<Track> getAlbumTrackListMbid(Connection connection, String mbid);

    List<TrackInfo> getAlbumInfoByName(Connection connection, List<UrlCapsule> urlCapsules);

    void getAlbumInfoByMbid(Connection connection, List<UrlCapsule> urlCapsules);

    ArtistMusicBrainzDetails getArtistInfo(Connection connection, ArtistInfo artistInfo);

    List<CountWrapper<AlbumInfo>> getYearAlbumsByReleaseNameLowerCaseAverage(Connection connection, List<AlbumInfo> emptyMbid, Year year);

    List<CountWrapper<AlbumInfo>> getYearAlbumsByReleaseNameAverage(Connection connection, List<AlbumInfo> releaseInfo, Year year);

    List<AlbumInfo> getDecadeAlbums(Connection connection, List<AlbumInfo> mbiz, int decade, int numberOfYears);

    List<CountWrapper<AlbumInfo>> getDecadeAverage(Connection connection, List<AlbumInfo> mbiz, int decade, int numberOfYears);

    List<CountWrapper<AlbumInfo>> getYearAlbumsByReleaseNameAverageDecade(Connection connection, List<AlbumInfo> emptyMbid, int decade, int numberOfYears);

    Map<Language, Long> getScriptLanguages(Connection connection, List<AlbumInfo> mbiz);


    List<AlbumGenre> getAlbumRecommendationsByGenre(Connection connection, Map<Genre, Integer> map, List<ScrobbledArtist> recs);
}
