package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

public class GenreParameters extends ChuuDataParams {
    private final String genre;
    private final boolean autoDetected;
    private final NowPlayingArtist np;
    private final User user;

    public GenreParameters(Context e, String genre, boolean autoDetected, @Nullable NowPlayingArtist np, @Nullable LastFMData lastFMData, User user) {
        super(e, lastFMData);
        this.genre = genre;
        this.autoDetected = autoDetected;
        this.np = np;
        this.user = user;
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

    public User getUser() {
        return user;
    }
}
