package core.music.radio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.Chuu;
import dao.entities.RandomTarget;
import dao.entities.RandomUrlEntity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public record RandomRadio(String name, long guildId, boolean onlyServer) implements RadioSource {

    public RandomRadio(long guildId, boolean onlyServer) {
        this("Random radio", guildId, onlyServer);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CompletableFuture<AudioTrack> nextTrack(RadioTrackContext context) {

        return this.nextTrack0(context, new AtomicInteger(1));
    }

    @Override
    public void serialize(ByteArrayOutputStream stream) throws IOException {
        var writer = new DataOutputStream(stream);
        writer.writeInt(4);
        writer.writeLong(guildId);
        writer.writeBoolean(onlyServer);
        writer.close(); // This invokes flush.
    }

    private CompletableFuture<AudioTrack> nextTrack0(RadioTrackContext context, AtomicInteger attempts) {
        if (attempts.get() > 3) {
            return CompletableFuture.completedFuture(null);
        }

        RandomUrlEntity randomUrl;
        int youtubeSkipAttemps = 0;
        do {
            youtubeSkipAttemps++;
            if (onlyServer && guildId != -1L) {
                randomUrl = Chuu.getDb().getRandomUrlFromServer(guildId, RandomTarget.SPOTIFY);
            } else {
                randomUrl = Chuu.getDb().getRandomUrl(RandomTarget.SPOTIFY);
            }
        } while (randomUrl.url().startsWith("https://www.youtube.com") && youtubeSkipAttemps <= 5);

        var future = new CompletableFuture<AudioTrack>();

        RandomUrlEntity finalRandomUrl = randomUrl;
        Chuu.playerManager.loadItemOrdered(this, randomUrl.url(), RetriableLoader.getLoader(future, true, () -> new RandomRadioTrackContext(context, this, finalRandomUrl.url(), finalRandomUrl.discordId()), context, attempts, this::nextTrack0, new HashSet<>()));
        return future;
    }
}
