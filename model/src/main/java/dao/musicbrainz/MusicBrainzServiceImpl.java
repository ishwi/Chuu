package dao.musicbrainz;

import com.neovisionaries.i18n.CountryCode;
import dao.MbizDatasource;
import dao.entities.*;
import dao.exceptions.ChuuServiceException;
import org.apache.commons.collections4.MultiValuedMap;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

public class MusicBrainzServiceImpl implements MusicBrainzService {
    private final MbizDatasource dataSource;
    private final MbizQueriesDao mbizQueriesDao;

    public MusicBrainzServiceImpl() {
        this.dataSource = new MbizDatasource(false);
        mbizQueriesDao = new MbizQueriesDaoImpl();
    }


    @Override
    public List<AlbumInfo> listOfYearReleases(List<AlbumInfo> mbiz, Year year) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (mbiz.isEmpty()) {
                return Collections.emptyList();
            }
            return mbizQueriesDao.getYearAlbums(connection, mbiz, year);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<AlbumInfo> listOfYearRangeReleases(List<AlbumInfo> mbiz, int baseYear, int numberOfYears) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (mbiz.isEmpty()) {
                return Collections.emptyList();
            }
            return mbizQueriesDao.getDecadeAlbums(connection, mbiz, baseYear, numberOfYears);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<CountWrapper<AlbumInfo>> listOfYearReleasesWithAverage(List<AlbumInfo> mbiz, Year year) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (mbiz.isEmpty()) {
                return Collections.emptyList();
            }
            return mbizQueriesDao.getYearAverage(connection, mbiz, year);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<CountWrapper<AlbumInfo>> listOfRangeYearReleasesWithAverage(List<AlbumInfo> mbiz, int baseYear, int numberOfYears) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (mbiz.isEmpty()) {
                return Collections.emptyList();
            }
            return mbizQueriesDao.getDecadeAverage(connection, mbiz, baseYear, numberOfYears);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<AlbumInfo> listOfCurrentYear(List<AlbumInfo> mbiz) {
        return this.listOfYearReleases(mbiz, Year.now());
    }

    @Override
    public List<AlbumInfo> findArtistByRelease(List<AlbumInfo> releaseInfo, Year year) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (releaseInfo.isEmpty()) {
                return Collections.emptyList();
            }

            return mbizQueriesDao.getYearAlbumsByReleaseName(connection, releaseInfo, year);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<AlbumInfo> findArtistByReleaseRangeYear(List<AlbumInfo> releaseInfo, int baseYear, int numberOfYears) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (releaseInfo.isEmpty()) {
                return Collections.emptyList();
            }

            return mbizQueriesDao.getDecadeAlbumsByReleaseName(connection, releaseInfo, baseYear, numberOfYears);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<AlbumInfo> findArtistByReleaseLowerCase(List<AlbumInfo> releaseInfo, Year year) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return mbizQueriesDao.getYearAlbumsByReleaseNameLowerCase(connection, releaseInfo, year);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<AlbumInfo> findArtistByReleaseCurrentYear(List<AlbumInfo> releaseInfo) {
        return null;
    }

    @Override
    public Map<Genre, List<AlbumInfo>> genreCount(List<AlbumInfo> releaseInfo) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            Map<String, AlbumInfo> missing = new HashMap<>();
            if (releaseInfo.isEmpty()) {
                return new HashMap<>();
            }
            Map<String, AlbumInfo> mbidIndexMap = releaseInfo.stream().collect(Collectors.toMap(EntityInfo::getMbid, x -> x, (x, y) -> {
                missing.put(y.getMbid(), y);
                return x;
            }));
            MultiValuedMap<Genre, String> genresAndMbids = mbizQueriesDao.genreCount(connection, releaseInfo);
            return genresAndMbids.asMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (Map.Entry<Genre, Collection<String>> k) -> k.getValue().stream().map(key -> {
                AlbumInfo ai = mbidIndexMap.get(key);
                if (ai == null) {
                    return missing.get(key);
                }
                return ai;
            }).toList()));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public Map<Genre, List<ArtistInfo>> genreCountByartist(List<ArtistInfo> releaseInfo) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            Map<String, ArtistInfo> missing = new HashMap<>();
            Map<String, ArtistInfo> mbidIndexMap = releaseInfo.stream().collect(Collectors.toMap(EntityInfo::getMbid, x -> x, (x, y) -> {
                missing.put(y.getMbid(), y);
                return x;
            }));
            MultiValuedMap<Genre, String> genresAndMbids = mbizQueriesDao.genreCountByArtist(connection, releaseInfo);
            return genresAndMbids.asMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (Map.Entry<Genre, Collection<String>> k) -> k.getValue().stream().map(key -> {
                ArtistInfo artistInfo = mbidIndexMap.get(key);
                if (artistInfo == null) {
                    return missing.get(key);
                }
                return artistInfo;
            }).toList()));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Set<String> albumsGenre(List<AlbumInfo> releaseInfo, String genre) {


        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (releaseInfo.isEmpty()) {
                return new HashSet<>();
            }
            return mbizQueriesDao.getAlbumsOfGenre(connection, genre, releaseInfo);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public List<AlbumInfo> albumsGenreByName(List<AlbumInfo> releaseInfo, String genre) {


        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (releaseInfo.isEmpty()) {
                return Collections.emptyList();
            }
            return mbizQueriesDao.getAlbumsOfGenreByName(connection, releaseInfo, genre);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public Map<Country, Integer> countryCount(List<ArtistInfo> artistInfo) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return mbizQueriesDao.countryCount(connection, artistInfo);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<ScrobbledArtist> getArtistFromCountry(CountryCode country, List<ScrobbledArtist> queue, Long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);

            List<ArtistInfo> artistInfos = queue.stream()
                    .map(capsule -> new ArtistInfo(capsule.getUrl(), capsule.getArtist(), capsule.getArtistMbid()))
                    .filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
                    .toList();
            if (artistInfos.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> artistFromCountry = mbizQueriesDao.getArtistFromCountry(connection, country, artistInfos);

            return queue.stream().filter(u -> u.getArtistMbid() != null && !u.getArtistMbid().isEmpty() && artistFromCountry.contains(u.getArtistMbid()))
                    .map(x -> new ScrobbledArtist(x.getArtist(), x.getCount(), x.getUrl())).toList();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }


    @Override
    public List<Track> getAlbumTrackListMbid(String mbid) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return mbizQueriesDao.getAlbumTrackListMbid(connection, mbid);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<Track> getAlbumTrackList(String artist, String album) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return mbizQueriesDao.getAlbumTrackList(connection, artist, album);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<Track> getAlbumTrackListLowerCase(String artist, String album) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return mbizQueriesDao.getAlbumTrackListLower(connection, artist, album);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public ArtistMusicBrainzDetails getArtistDetails(ArtistInfo artist) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return mbizQueriesDao.getArtistInfo(connection, artist);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<TrackInfo> getAlbumInfoByNames(List<AlbumInfo> albumInfos) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return mbizQueriesDao.getAlbumInfoByName(connection, albumInfos);
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void getAlbumInfoByMbid(List<ScrobbledAlbum> urlCapsules) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            mbizQueriesDao.getAlbumInfoByMbid(connection, urlCapsules);
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public MusicbrainzFullAlbumEntity getAlbumInfo(FullAlbumEntityExtended albumInfo) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (albumInfo.getMbid() == null || albumInfo.getMbid().isBlank()) {
                return new MusicbrainzFullAlbumEntity(albumInfo, Collections.emptyList(), null);
            }
            return mbizQueriesDao.retrieveAlbumInfo(connection, albumInfo);
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public List<CountWrapper<AlbumInfo>> findArtistByReleaseLowerCaseWithAverage(List<AlbumInfo> emptyMbid, Year year) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return mbizQueriesDao.getYearAlbumsByReleaseNameLowerCaseAverage(connection, emptyMbid, year);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public List<CountWrapper<AlbumInfo>> findArtistByReleaseWithAverage(List<AlbumInfo> releaseInfo, Year year) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (releaseInfo.isEmpty()) {
                return Collections.emptyList();
            }

            return mbizQueriesDao.getYearAlbumsByReleaseNameAverage(connection, releaseInfo, year);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<CountWrapper<AlbumInfo>> findArtistByReleaseWithAverageRangeYears(List<AlbumInfo> emptyMbid, int baseYear, int numberOfYears) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (emptyMbid.isEmpty()) {
                return Collections.emptyList();
            }

            return mbizQueriesDao.getYearAlbumsByReleaseNameAverageDecade(connection, emptyMbid, baseYear, numberOfYears);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Map<Language, Long> getLanguageCountByMbid(List<AlbumInfo> withMbid) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (withMbid.isEmpty()) {
                return new HashMap<>();
            }

            return mbizQueriesDao.getScriptLanguages(connection, withMbid);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<AlbumGenre> getAlbumRecommendationsByGenre(Map<Genre, Integer> map, List<ScrobbledArtist> recs) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (map.isEmpty() || recs.isEmpty()) {
                return Collections.emptyList();
            }

            return mbizQueriesDao.getAlbumRecommendationsByGenre(connection, map, recs);

        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public Set<String> artistGenres(List<ArtistInfo> artists, String genre) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (artists.isEmpty()) {
                return new HashSet<>();
            }
            return mbizQueriesDao.getArtistOfGenre(connection, genre, artists);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }
}
