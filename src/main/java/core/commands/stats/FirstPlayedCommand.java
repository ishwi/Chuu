package core.commands.stats;

import core.commands.Context;
import core.commands.albums.AlbumPlaysCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class FirstPlayedCommand extends AlbumPlaysCommand {
    public FirstPlayedCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistSongParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "First time you scrobbled a song";
    }

    @Override
    public List<String> getAliases() {
        return List.of("firsttrack", "firstsong", "firsttr");
    }

    @Override
    public String getName() {
        return "First song scrobbled";
    }

    @Override
    protected void doSomethingWithAlbumArtist(ScrobbledArtist artist, String song, Context e, long who, ArtistAlbumParameters params) {
        LastFMData lastFMData = params.getLastFMData();

        Optional<Instant> instant = db.getFirstScrobbled(artist.getArtistId(), song, params.getLastFMData().getName());
        if (instant.isEmpty()) {
            sendMessageQueue(e, "Couldn't get the first time you scrobbled **" + song + "** by _" + artist.getArtist() + "_");
            return;
        }
        String usernameString = getUserString(e, who, lastFMData.getName());
        String date = CommandUtil.getDateTimestampt(instant.get(), TimeFormat.RELATIVE);
        sendMessageQueue(e, String.format("First time that **%s** scrobbled **%s** was %s", usernameString, CommandUtil.escapeMarkdown(song), date));
    }
}

