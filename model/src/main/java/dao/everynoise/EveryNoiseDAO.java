package dao.everynoise;

import dao.entities.NoiseGenre;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EveryNoiseDAO {
    List<NoiseGenre> fuzzyMatch(Connection connection, String genre);


    List<Release> releasesOfGenre(Connection connection, LocalDate week, NoiseGenre noiseGenre);

    List<NoiseGenre> listAll(Connection connection);

    void insertGenres(Connection connection, List<NoiseGenre> genres);

    Map<Release, Long> insertReleases(Connection connection, List<Release> release, LocalDate week);


    void insertGenreRelases(Connection connection, Map<Long, Set<String>> idToGenres);

}
