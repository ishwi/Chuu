package core.music.scrobble;

import core.music.utils.TrackScrobble;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.time.Instant;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public record ScrobbleStatus(ScrobbleStates scrobbleStatus, TrackScrobble scrobble,
                             Supplier<VoiceChannel> voiceChannelSupplier,
                             long channelId, Instant moment,
                             BiConsumer<ScrobbleStatus,
                                     Set<LastFMData>> callback, ExtraParams extraParams) {

    public ScrobbleStatus(ScrobbleStates scrobbleStatus, TrackScrobble scrobble, Supplier<VoiceChannel> voiceChannelSupplier, long channelId, Instant moment, BiConsumer<ScrobbleStatus,
            Set<LastFMData>> callback) {
        this(scrobbleStatus, scrobble, voiceChannelSupplier, channelId, moment, callback, new EmptyParams());
    }


}
