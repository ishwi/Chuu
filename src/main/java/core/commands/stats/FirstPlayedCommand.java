package core.commands.stats;

import core.commands.albums.AlbumPlaysCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public class FirstPlayedCommand extends AlbumPlaysCommand {
    public FirstPlayedCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistSongParser(getService(), lastFM);
    }

    @Override
    public String getDescription() {
        return "First time you scrobbled a song";
    }

    @Override
    public List<String> getAliases() {
        return List.of("lasttrack", "lastsong", "lasttr");
    }

    @Override
    public String getName() {
        return "First song scrobbled";
    }

    @Override
    protected void doSomethingWithAlbumArtist(ScrobbledArtist artist, String song, MessageReceivedEvent e, long who, ArtistAlbumParameters params) {
        LastFMData lastFMData = params.getLastFMData();

        Optional<Instant> instant = getService().getFirstScrobbled(artist.getArtistId(), song, params.getLastFMData().getName());
        if (instant.isEmpty()) {
            sendMessageQueue(e, "Couldn't get the first time you scrobbled **" + song + "** by _" + artist.getArtist() + "_");
            return;
        }
        String usernameString = getUserString(e, who, lastFMData.getName());
        OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant.get(), lastFMData.getTimeZone().toZoneId());
        String date = CommandUtil.getAmericanizedDate(offsetDateTime);
        sendMessageQueue(e, String.format("First time that **%s** scrobbled **%s** was at %s", usernameString, CommandUtil.cleanMarkdownCharacter(song), date));
    }
}

