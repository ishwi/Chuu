package dao.everynoise;

import dao.SimpleDataSource;
import dao.entities.NoiseGenre;
import dao.exceptions.ChuuServiceException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public List<Release> releasesOfGenre(LocalDate localDate, NoiseGenre genre) {
        if (localDate.getDayOfWeek() != DayOfWeek.FRIDAY) {
            return new ArrayList<>();
        }
        try (Connection connection = ds.getConnection()) {
            connection.setReadOnly(true);
            return everyNoiseDAO.releasesOfGenre(connection, localDate, genre);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<NoiseGenre> listAll() {
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


}
