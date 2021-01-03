package core.music.radio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.Chuu;
import dao.entities.RandomUrlEntity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class RandomRadio implements RadioSource {
    private final String name;
    private final Long guildId;

    public RandomRadio(String name, Long guildId) {
        this.name = name;
        this.guildId = guildId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CompletableFuture<AudioTrack> nextTrack(RadioTrackContext context) {

        return this.nextTrack0(context, 1);
    }

    @Override
    public void serialize(ByteArrayOutputStream stream) throws IOException {
        var writer = new DataOutputStream(stream);
        writer.writeInt(3);
        writer.writeUTF(name);
        writer.writeLong(guildId);
        writer.close(); // This invokes flush.
    }

    private CompletableFuture<AudioTrack> nextTrack0(RadioTrackContext context, int attempts) {
        if (attempts > 3) {
            return CompletableFuture.completedFuture(null);
        }

        RandomUrlEntity randomUrl;
        int youtubeSkipAttemps = 0;
        do {
            youtubeSkipAttemps++;
            if (guildId == null) {
                randomUrl = Chuu.getDao().getRandomUrl();
            } else {
                randomUrl = Chuu.getDao().getRandomUrlFromServer(guildId);
            }
        } while (randomUrl.getUrl().startsWith("https://www.youtube.com") && youtubeSkipAttemps <= 5);

        var future = new CompletableFuture<AudioTrack>();

        Chuu.playerManager.loadItemOrdered(this, randomUrl.getUrl(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(context);
                future.complete(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                trackLoaded(playlist.getTracks().get(0));
            }

            @Override
            public void noMatches() {
                future.complete(null);

            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if (attempts >= 3) {
                    future.complete(null);
                } else {
                    nextTrack0(context, attempts + 1);
                }
            }

        });

        return future;
    }
}
