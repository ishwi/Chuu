package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.utils.TimeFormat;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public class FirstArtistCommand extends ConcurrentCommand<ArtistParameters> {
    public FirstArtistCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "First time you scrobbled an artist";
    }

    @Override
    public List<String> getAliases() {
        return List.of("first", "firstscrobbled");
    }

    @Override
    public String getName() {
        return "First scrobble of an artist";
    }

    @Override
    protected void onCommand(Context e, @Nonnull ArtistParameters params) throws LastFmException {
        ScrobbledArtist artist = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), false, !params.isNoredirect());
        long whom = params.getLastFMData().getDiscordId();
        int a;
        LastFMData data = params.getLastFMData();
        Optional<Instant> instant = db.getFirstScrobbledArtist(artist.getArtistId(), params.getLastFMData().getName());
        if (instant.isEmpty()) {
            sendMessageQueue(e, "Couldn't get the first time you scrobbled **" + artist.getArtist() + "**");
            return;
        }
        String usernameString = getUserString(e, params.getLastFMData().getDiscordId(), data.getName());
        OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant.get(), data.getTimeZone().toZoneId());
        String date = CommandUtil.getDateTimestampt(instant.get(), TimeFormat.RELATIVE);
        sendMessageQueue(e, String.format("First time that **%s** scrobbled **%s** was %s", usernameString, CommandUtil.escapeMarkdown(artist.getArtist()), date));
    }


}

