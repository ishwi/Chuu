package core.commands.stats;

import core.commands.Context;
import core.commands.albums.AlbumPlaysCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.ArtistSongParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public class LastPlayedCommand extends AlbumPlaysCommand {
    public LastPlayedCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        ArtistSongParser artistSongParser = new ArtistSongParser(db, lastFM);
        artistSongParser.addOptional(new OptionalEntity("today", "to not include the current day"));
        return artistSongParser;
    }

    @Override
    public String getDescription() {
        return "Last time you scrobbled a song";
    }

    @Override
    public List<String> getAliases() {
        return List.of("lasttrack", "lastsong", "lasttr");
    }

    @Override
    public String getName() {
        return "Last song scrobbled";
    }

    @Override
    protected void doSomethingWithAlbumArtist(ScrobbledArtist artist, String song, Context e, long who, ArtistAlbumParameters params) {
        LastFMData lastFMData = params.getLastFMData();

        Optional<Instant> instant = db.getLastScrobbled(artist.getArtistId(), song, params.getLastFMData().getName(), params.hasOptional("today"));
        if (instant.isEmpty()) {
            sendMessageQueue(e, "Couldn't get the last time you scrobbled **" + song + "** by _" + artist.getArtist() + "_");
            return;
        }
        String usernameString = getUserString(e, who, lastFMData.getName());
        OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant.get(), lastFMData.getTimeZone().toZoneId());
        String date = CommandUtil.getAmericanizedDate(offsetDateTime);
        sendMessageQueue(e, String.format("Last time that **%s** scrobbled **%s** was at %s", usernameString, CommandUtil.cleanMarkdownCharacter(song), date));
    }
}

