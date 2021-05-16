package dao.everynoise;

import dao.entities.NoiseGenre;

import java.time.LocalDate;
import java.util.List;

public interface EveryNoiseService {

    List<NoiseGenre> findMatchingGenre(String genre);


    List<Release> releasesOfGenre(LocalDate localDate, NoiseGenre genre);

    List<NoiseGenre> listAll();

    void insertReleases(List<ReleaseWithGenres> release, LocalDate week);

    void insertGenres(List<NoiseGenre> genres);

}
