package core.music.scrobble;

import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.apis.last.entities.Scrobble;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.entities.ISnowflake;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StatusProcesser {
    private static final Map<Identifier, ScrobbleStatus> statuses = new HashMap<>();
    private static final Map<Identifier, ScrobbleStatus> overriden = new HashMap<>();
    private final ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
    private final ChuuService db;

    public StatusProcesser(ChuuService db) {
        this.db = db;
    }

    private static Identifier genId(ScrobbleStatus status) {
        return new Identifier(status.channelId(), status.scrobble().uuid());
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

    private void processMetadataChange(ScrobbleStatus next) throws LastFmException {
        ScrobbleStatus previous = statuses.remove(genId(next));
        if (previous == null) {
            Chuu.getLogger().info("Something went wrong metadata, didnt pexists previous");
        } else {
            Long duration = next.scrobble().scrobble().duration();
            processStart(new ScrobbleStatus(ScrobbleStates.SCROBBLING,
                    next.scrobble()
                    , next.voiceChannelSupplier(), next.channelId(), previous.moment(), next.callback(), next.extraParams()));
        }

    }

    private void processStart(ScrobbleStatus status) throws LastFmException {
        ScrobbleStatus previous = statuses.put(genId(status), status);
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
        if (Chuu.chuuSess != null) {
            lastFM.flagNP(Chuu.chuuSess, scrobble);
        }
        for (LastFMData data : scrobbleableUsers) {
            lastFM.flagNP(data.getSession(), scrobble);
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

    private void processEnd(ScrobbleStatus status) throws LastFmException {
        ScrobbleStatus first = statuses.remove(genId(status));
        if (first == null) {
            first = overriden.remove(genId(status));
            if (first == null) {
                CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(() -> {
                    if (statuses.containsKey(genId(status)) || overriden.containsKey(genId(status))) {
                        try {
                            this.processEnd(status);
                        } catch (LastFmException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Chuu.getLogger().warn("Was not able to remove scrobble {}", status);
                    }
                });
                return;
            }
        }

        long seconds = status.moment().getEpochSecond() - first.moment().getEpochSecond();
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

        for (LastFMData data : scrobbleableUsers) {
            lastFM.scrobble(data.getSession(), scrobble, first.moment());
        }
        if (Chuu.chuuSess != null) {
            lastFM.scrobble(Chuu.chuuSess, scrobble, first.moment());
        }
        status.callback().accept(status, scrobbleableUsers);
    }

    private record Identifier(long channelId, UUID uuid) {
    }
}
