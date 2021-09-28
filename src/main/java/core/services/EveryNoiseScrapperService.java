package core.services;

import core.Chuu;
import core.music.everynoise.EveryNoiseScrapper;
import dao.everynoise.EveryNoiseService;
import dao.everynoise.NoiseGenre;
import dao.everynoise.ReleaseWithGenres;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;

public class EveryNoiseScrapperService {
    private final EveryNoiseScrapper everyNoiseScrapper;
    private final EveryNoiseService everyNoiseService;

    public EveryNoiseScrapperService(EveryNoiseScrapper everyNoiseScrapper, EveryNoiseService everyNoiseService) {
        this.everyNoiseScrapper = everyNoiseScrapper;
        this.everyNoiseService = everyNoiseService;
    }

    public void scrapeGenres() {
        try {
            List<NoiseGenre> genres = everyNoiseScrapper.scrapeGenres();
            this.everyNoiseService.insertGenres(genres);
            Chuu.getLogger().info("Inserted {} genres ", genres.size());
        } catch (IOException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
    }

    public void scrapeReleases(LocalDate week) {
        List<NoiseGenre> dbGenres = everyNoiseService.listAllGenres();

        try {
            List<ReleaseWithGenres> genres = everyNoiseScrapper.scrape(dbGenres.stream().map(NoiseGenre::name).toList(), week);
            everyNoiseService.insertReleases(genres, week);
            Chuu.getLogger().info("Inserted {} releases for week {}", genres.size(), week);
        } catch (UncheckedIOException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
    }

}
