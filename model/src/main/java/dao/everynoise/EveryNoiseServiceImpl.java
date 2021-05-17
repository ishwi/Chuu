package dao.everynoise;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import dao.SimpleDataSource;
import dao.exceptions.ChuuServiceException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class EveryNoiseServiceImpl implements EveryNoiseService {
    private final SimpleDataSource ds;
    private final EveryNoiseDAO everyNoiseDAO;

    public EveryNoiseServiceImpl(SimpleDataSource ds) {
        this.ds = ds;
        this.everyNoiseDAO = new EveryNoiseDAOImpl();
    }

    @Override
    public List<NoiseGenre> findMatchingGenre(String genre) {
        try (Connection connection = ds.getConnection()) {
            connection.setReadOnly(true);
            return everyNoiseDAO.fuzzyMatch(connection, genre);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<Release> releasesOfGenre(NoiseGenre genre) {
        try (Connection connection = ds.getConnection()) {
            connection.setReadOnly(true);
            return everyNoiseDAO.releasesOfGenre(connection, genre);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<Release> allValidReleases() {
        try (Connection connection = ds.getConnection()) {
            connection.setReadOnly(true);
            return everyNoiseDAO.allValidRelease(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Map<NoiseGenreReleases, Integer> releasesByCount() {
        try (Connection connection = ds.getConnection()) {
            connection.setReadOnly(true);
            ObjectReader objectReader = new ObjectMapper().readerForListOf(Release.class);
            return everyNoiseDAO.releasesByCount(connection)
                    .entrySet().stream()
                    .collect(Collectors.toMap(z -> {
                        NoiseGenreJSON key = z.getKey();
                        List<Release> releases;
                        try {
                            // Probably doing something wrong here????
                            // Windows returns and jsonobject
                            // debian returns a jsonArray??? idek
                            // TODO
                            if (!key.jsonReleases().startsWith("[")) {
                                releases = objectReader.readValue("[" + key.jsonReleases() + "]");
                            } else {
                                releases = objectReader.readValue(key.jsonReleases());

                            }
                        } catch (JsonProcessingException e) {
                            releases = Collections.emptyList();
                        }
                        return new NoiseGenreReleases(key.genre(), key.playlist(), releases);
                    }, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<NoiseGenre> listAllGenres() {
        try (Connection connection = ds.getConnection()) {
            connection.setReadOnly(true);
            return everyNoiseDAO.listAll(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertReleases(List<ReleaseWithGenres> release, LocalDate week) {
        try (Connection connection = ds.getConnection()) {
            Map<Release, ReleaseWithGenres> releases = release.stream().collect(Collectors.toMap(z -> new Release(z.artist(), z.release(), z.href()), z -> z));
            Map<Release, Long> releaseLongMap = everyNoiseDAO.insertReleases(connection, new ArrayList<>(releases.keySet()), week);
            Map<Long, Set<String>> idGenres = releaseLongMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue
                    , z ->
                            releases.get(z.getKey()).genre()
            ));
            everyNoiseDAO.insertGenreRelases(connection, idGenres);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertGenres(List<NoiseGenre> genres) {
        try (Connection connection = ds.getConnection()) {
            if (genres.isEmpty()) {
                return;
            }
            everyNoiseDAO.insertGenres(connection, genres);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Optional<NoiseGenre> findExactMatch(String genre) {
        try (Connection connection = ds.getConnection()) {
            connection.setReadOnly(true);
            return everyNoiseDAO.findExactMatch(connection, genre);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


}
