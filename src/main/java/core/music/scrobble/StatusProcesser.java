package core.music.scrobble;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.apis.last.entities.Scrobble;
import core.exceptions.LastFmException;
import core.util.ChuuVirtualPool;
import core.util.VirtualParallel;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.entities.ISnowflake;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StatusProcesser {
    private static final Cache<Identifier, ScrobbleStatus> statuses = Caffeine.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES).build();
    private static final Cache<Identifier, ScrobbleStatus> overriden = Caffeine.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES).maximumSize(500L).build();
    private static final Executor scrobbleRequester = ChuuVirtualPool.of("Scrobbler-Manager");
    private final ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
    private final ChuuService db;

    public StatusProcesser(ChuuService db) {
        this.db = db;
    }

    private static Identifier genId(ScrobbleStatus status) {
        return new Identifier(status.guildId(), status.scrobble().uuid());
    }


    public void process(ScrobbleStatus next) throws Exception {
        ScrobbleStates scrobbleStatus = next.scrobbleStatus();
        switch (scrobbleStatus) {
            case SCROBBLING -> processStart(next);
            case FINISHED -> processEnd(next);
            case METADATA_CHANGE -> processMetadataChange(next);
            case CHAPTER_CHANGE -> processChapterChange(next);
        }
    }

    private void processChapterChange(ScrobbleStatus next) throws LastFmException {
        processEnd(next);
        processStart(next);
    }

    private ScrobbleStatus evict(ScrobbleStatus next) {
        Identifier key = genId(next);
        ScrobbleStatus status = statuses.getIfPresent(key);
        if (status != null) {
            statuses.invalidate(key);
            return status;
        }
        return null;
    }

    private ScrobbleStatus evictOverriden(ScrobbleStatus next) {
        Identifier key = genId(next);
        ScrobbleStatus status = overriden.getIfPresent(key);
        if (status != null) {
            statuses.invalidate(key);
            return status;
        }
        return null;
    }

    private void processMetadataChange(ScrobbleStatus next) throws LastFmException {
        ScrobbleStatus previous = evict(next);
        if (previous == null) {
            Chuu.getLogger().info("Something went wrong metadata, didnt exists previous");
            processStart(new ScrobbleStatus(ScrobbleStates.SCROBBLING,
                    next.scrobble()
                    , next.voiceChannelSupplier(), next.guildId(), next.moment(), next.callback(), next.extraParams()));
        } else {
            processStart(new ScrobbleStatus(ScrobbleStates.SCROBBLING,
                    next.scrobble()
                    , next.voiceChannelSupplier(), next.guildId(), previous.moment(), next.callback(), next.extraParams()));
        }
    }

    private void processStart(ScrobbleStatus status) {
        ScrobbleStatus previous = statuses.get(genId(status), (k) -> status);
        if (previous != null) {
            overriden.put(genId(previous), previous);
        }
        Set<LastFMData> scrobbleableUsers = getUsers(status);

        Scrobble scrobble;
        if (status.extraParams() instanceof ExtraParamsChapter epc) {
            scrobble = status.scrobble().scrobble(epc.currentMs(), epc.totalMs());
        } else {
            scrobble = status.scrobble().scrobble();
        }


        try (var scope = new VirtualParallel.ExecuteAllIgnoreErrors<Void>()) {
            for (LastFMData z : scrobbleableUsers) {
                scope.fork(() -> {
                    lastFM.flagNP(z.getSession(), scrobble);
                    return null;
                });
            }
            if (Chuu.chuuSess != null) {
                scope.fork(() -> {
                    lastFM.flagNP(Chuu.chuuSess, scrobble);
                    return null;
                });
            }
            scope.join();

        } catch (InterruptedException e) {
            Chuu.getLogger().debug("Error flagging np", e);
        }

        status.callback().accept(status, scrobbleableUsers);
    }

    @Nonnull
    private Set<LastFMData> getUsers(ScrobbleStatus status) {
        var channel = status.voiceChannelSupplier().get();
        if (channel == null) return Collections.emptySet();
        List<Long> voiceMembers = channel.getMembers().stream().mapToLong(ISnowflake::getIdLong).boxed().toList();
        return db.findScrobbleableUsers(channel.getGuild().getIdLong()).stream()
                .filter(x -> voiceMembers.contains(x.getDiscordId())).collect(Collectors.toSet());
    }

    private void processEnd(ScrobbleStatus status) {
        ScrobbleStatus first = evict(status);
        if (first == null) {
            first = evictOverriden(status);
            if (first == null) {
                Chuu.getLogger().warn("Was not able to remove scrobble {}", status);
//                return;
            }

        }
        long seconds;
        Instant moment;
        if (first == null) {
            moment = status.moment();
            seconds = Instant.now().getEpochSecond() - status.moment().getEpochSecond();
        } else {
            moment = first.moment();
            seconds = status.moment().getEpochSecond() - first.moment().getEpochSecond();
        }
        if (seconds < 30 || (seconds < (status.scrobble().scrobble().duration() / 1000) / 2 && seconds < 4 * 60)) {
            Chuu.getLogger().info("Didnt scrobble {}: Duration {}", status.scrobble().identifier(), seconds);
            return;
        }
        Set<LastFMData> scrobbleableUsers = getUsers(status);

        Scrobble scrobble;
        if (status.extraParams() instanceof ExtraParamsChapter epc) {
            scrobble = status.scrobble().scrobble(epc.baseLineMs() - 1, epc.totalMs());
        } else {
            scrobble = status.scrobble().scrobble();
        }

        try (var scope = new VirtualParallel.ExecuteAllIgnoreErrors<Void>()) {
            for (LastFMData z : scrobbleableUsers) {
                scope.fork(() -> {
                    lastFM.scrobble(z.getSession(), scrobble, moment);
                    return null;
                });
            }
            if (Chuu.chuuSess != null) {
                scope.fork(() -> {
                    lastFM.scrobble(Chuu.chuuSess, scrobble, moment);
                    return null;
                });
            }
            scope.join();

        } catch (InterruptedException e) {
            Chuu.getLogger().debug("Error flagging np", e);
        }


        status.callback().accept(status, scrobbleableUsers);
    }

    private record Identifier(long guildId, java.util.UUID identifier) {
    }
}
