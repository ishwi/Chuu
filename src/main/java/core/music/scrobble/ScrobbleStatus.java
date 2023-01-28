package core.music.scrobble;

import core.music.utils.TrackScrobble;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

import java.time.Instant;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public record ScrobbleStatus(ScrobbleStates scrobbleStatus, TrackScrobble scrobble,
                             Supplier<AudioChannel> voiceChannelSupplier,
                             long guildId, Instant moment,
                             BiConsumer<ScrobbleStatus,
                                     Set<LastFMData>> callback, ExtraParams extraParams) {

    public ScrobbleStatus(ScrobbleStates scrobbleStatus, TrackScrobble scrobble, Supplier<AudioChannel> voiceChannelSupplier, long guildId, Instant moment, BiConsumer<ScrobbleStatus,
            Set<LastFMData>> callback) {
        this(scrobbleStatus, scrobble, voiceChannelSupplier, guildId, moment, callback, new EmptyParams());
    }


}
