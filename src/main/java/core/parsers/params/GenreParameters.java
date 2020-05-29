package core.parsers.params;

import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

public class GenreParameters extends CommandParameters {
    private final String genre;
    private final boolean autoDetected;
    private final NowPlayingArtist np;

    public GenreParameters(MessageReceivedEvent e, String genre, boolean autoDetected, @Nullable NowPlayingArtist np) {
        super(e);
        this.genre = genre;
        this.autoDetected = autoDetected;
        this.np = np;
    }

    public String getGenre() {
        return genre;
    }

    public boolean isAutoDetected() {
        return autoDetected;
    }

    public NowPlayingArtist getNp() {
        return np;
    }
}
