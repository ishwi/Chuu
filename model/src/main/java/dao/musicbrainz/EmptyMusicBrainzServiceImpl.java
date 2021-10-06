package dao.musicbrainz;

import com.neovisionaries.i18n.CountryCode;
import dao.entities.*;

import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmptyMusicBrainzServiceImpl implements MusicBrainzService {
    @Override
    public List<AlbumInfo> listOfYearReleases(List<AlbumInfo> mbiz, Year year) {
        return Collections.emptyList();
    }

    @Override
    public List<AlbumInfo> listOfYearRangeReleases(List<AlbumInfo> mbiz, int baseYear, int numberOfYears) {
        return Collections.emptyList();

    }

    @Override
    public List<CountWrapper<AlbumInfo>> listOfYearReleasesWithAverage(List<AlbumInfo> mbiz, Year year) {
        return Collections.emptyList();

    }

    @Override
    public List<CountWrapper<AlbumInfo>> listOfRangeYearReleasesWithAverage(List<AlbumInfo> mbiz, int baseYear, int numberOfYears) {
        return Collections.emptyList();

    }

    @Override
    public List<AlbumInfo> listOfCurrentYear(List<AlbumInfo> mbiz) {
        return Collections.emptyList();

    }

    @Override
    public List<AlbumInfo> findArtistByRelease(List<AlbumInfo> releaseInfo, Year year) {
        return Collections.emptyList();

    }

    @Override
    public List<AlbumInfo> findArtistByReleaseRangeYear(List<AlbumInfo> releaseInfo, int baseYear, int numberOfYears) {
        return Collections.emptyList();

    }

    @Override
    public List<AlbumInfo> findArtistByReleaseLowerCase(List<AlbumInfo> releaseInfo, Year year) {
        return Collections.emptyList();

    }

    @Override
    public List<AlbumInfo> findArtistByReleaseCurrentYear(List<AlbumInfo> releaseInfo) {
        return Collections.emptyList();

    }

    @Override
    public Map<Genre, List<AlbumInfo>> genreCount(List<AlbumInfo> releaseInfo) {
        return Collections.emptyMap();

    }

    @Override
    public Map<Genre, List<ArtistInfo>> genreCountByartist(List<ArtistInfo> releaseInfo) {
        return Collections.emptyMap();

    }

    @Override
    public List<Track> getAlbumTrackList(String artist, String album) {
        return Collections.emptyList();

    }

    @Override
    public List<Track> getAlbumTrackListLowerCase(String artist, String album) {
        return Collections.emptyList();

    }

    @Override
    public Set<String> albumsGenre(List<AlbumInfo> releaseInfo, String genre) {
        return Collections.emptySet();

    }

    @Override
    public List<AlbumInfo> albumsGenreByName(List<AlbumInfo> releaseInfo, String genre) {
        return Collections.emptyList();

    }

    @Override
    public Map<Country, Integer> countryCount(List<ArtistInfo> artistInfo) {
        return Collections.emptyMap();

    }

    @Override
    public List<ScrobbledArtist> getArtistFromCountry(CountryCode country, List<ScrobbledArtist> allUserArtist, Long discordId) {
        return Collections.emptyList();

    }

    @Override
    public List<Track> getAlbumTrackListMbid(String mbid) {
        return Collections.emptyList();

    }

    @Override
    public ArtistMusicBrainzDetails getArtistDetails(ArtistInfo artist) {
        return null;

    }

    @Override
    public List<TrackInfo> getAlbumInfoByNames(List<AlbumInfo> albumInfos) {
        return Collections.emptyList();

    }

    @Override
    public void getAlbumInfoByMbid(List<ScrobbledAlbum> urlCapsules) {

    }

    @Override
    public MusicbrainzFullAlbumEntity getAlbumInfo(FullAlbumEntityExtended albumInfo) {
        return new MusicbrainzFullAlbumEntity(new FullAlbumEntityExtended(null, null, 0, null, null, 0, 0, Collections.emptyList(), null), Collections.emptyList(), null);

    }

    @Override
    public List<CountWrapper<AlbumInfo>> findArtistByReleaseLowerCaseWithAverage(List<AlbumInfo> emptyMbid, Year year) {
        return Collections.emptyList();

    }

    @Override
    public List<CountWrapper<AlbumInfo>> findArtistByReleaseWithAverage(List<AlbumInfo> emptyMbid, Year year) {
        return Collections.emptyList();

    }

    @Override
    public List<CountWrapper<AlbumInfo>> findArtistByReleaseWithAverageRangeYears(List<AlbumInfo> emptyMbid, int baseYear, int numberOfYears) {
        return Collections.emptyList();

    }

    @Override
    public Map<Language, Long> getLanguageCountByMbid(List<AlbumInfo> withMbid) {
        return Collections.emptyMap();

    }

    @Override
    public List<AlbumGenre> getAlbumRecommendationsByGenre(Map<Genre, Integer> map, List<ScrobbledArtist> recs) {
        return Collections.emptyList();

    }

    @Override
    public Set<String> artistGenres(List<ArtistInfo> artists, String genre) {
        return Collections.emptySet();

    }
}
