package dao.everynoise;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EveryNoiseService {

    List<NoiseGenre> findMatchingGenre(String genre);


    List<Release> releasesOfGenre(NoiseGenre genre);

    List<Release> allValidReleases();

    Map<NoiseGenreReleases, Integer> releasesByCount();

    List<NoiseGenre> listAllGenres();

    void insertReleases(List<ReleaseWithGenres> release, LocalDate week);

    void insertGenres(List<NoiseGenre> genres);

    Optional<NoiseGenre> findExactMatch(String genre);

}
