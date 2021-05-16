package core.services;

import core.music.everynoise.EveryNoiseScrapper;
import core.scheduledtasks.ArtistMbidUpdater;
import core.scheduledtasks.ImageUpdaterThread;
import core.scheduledtasks.SpotifyUpdaterThread;
import core.scheduledtasks.UpdaterThread;
import dao.ChuuService;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledService {
    private final ScheduledExecutorService scheduledExecutorService;
    private final ChuuService dao;

    public ScheduledService(ScheduledExecutorService scheduledExecutorService, ChuuService dao) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.dao = dao;
    }

    public void setScheduled() {
        scheduledExecutorService.scheduleAtFixedRate(
                new UpdaterThread(dao, true), 60, 120,
                TimeUnit.SECONDS);

        scheduledExecutorService.scheduleAtFixedRate(new ImageUpdaterThread(dao), 20, 12, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleAtFixedRate(
                new SpotifyUpdaterThread(dao), 5, 5, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleAtFixedRate(new ArtistMbidUpdater(dao), 100, 3600, TimeUnit.MINUTES);

        scheduleAtFridays(() -> new EveryNoiseScrapperService(new EveryNoiseScrapper(), dao).scrapeReleases(LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.FRIDAY))));
        scheduleEachMonth(() -> new EveryNoiseScrapperService(new EveryNoiseScrapper(), dao).scrapeGenres());

    }


    public void addSchedule(Runnable runnable, int delay, int period, TimeUnit minutes) {
        scheduledExecutorService.scheduleAtFixedRate(runnable, delay, period, minutes);
    }

    private void scheduleAtFridays(Runnable runnable) {
        LocalDateTime dt = LocalDateTime.now();
        long nextFridayAt18 = dt.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)).withHour(18).toEpochSecond(ZoneOffset.UTC);
        long delay = nextFridayAt18 - Instant.now().getEpochSecond();
        // Misses daylight changes but doesnt matter
        scheduledExecutorService.scheduleAtFixedRate(runnable, delay, 60 * 60 * 25 * 7, TimeUnit.SECONDS);
    }

    private void scheduleEachMonth(Runnable runnable) {
        LocalDateTime dt = LocalDateTime.now();
        long nextMonth = dt.plusMonths(1).toEpochSecond(ZoneOffset.UTC);
        long delay = nextMonth - Instant.now().getEpochSecond();
        // Misses daylight changes but doesnt matter
        scheduledExecutorService.scheduleAtFixedRate(runnable, delay, 60 * 60 * 25 * 31, TimeUnit.SECONDS);
    }

    public ScheduledFuture<?> addSchedule(Runnable o, long activeSeconds, TimeUnit seconds) {
        return scheduledExecutorService.schedule(o, activeSeconds, seconds);
    }
}
