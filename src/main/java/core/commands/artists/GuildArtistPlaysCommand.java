package core.commands.artists;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.validators.ArtistValidator;
import core.util.ServiceView;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;

import javax.annotation.Nonnull;
import java.util.List;

public class GuildArtistPlaysCommand extends ConcurrentCommand<ArtistParameters> {

    public GuildArtistPlaysCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Artist plays on this server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("serverplays", "sp");
    }

    @Override
    public String slashName() {
        return "plays";
    }

    @Override
    public String getName() {
        return "Artist Server Plays";
    }

    @Override
    public void onCommand(Context e, @Nonnull ArtistParameters params) throws LastFmException {

        String artist = params.getArtist();

        ScrobbledArtist scrobbledArtist = new ArtistValidator(db, lastFM, e).validate(artist, !params.isNoredirect());

        long artistPlays;
        if (e.isFromGuild()) {
            artistPlays = db.getServerArtistPlays(e.getGuild().getIdLong(), scrobbledArtist.getArtistId());
        } else {
            LastFMData data = params.getLastFMData();
            artistPlays = db.getArtistPlays(scrobbledArtist.getArtistId(), data.getName());
        }
        String usableString;
        if (e.isFromGuild()) {
            usableString = e.getGuild().getName();
        } else {
            usableString = e.getAuthor().getName();
        }
        usableString = CommandUtil.escapeMarkdown(usableString);
        sendMessageQueue(e, String.format("**%s** has **%d** %s on **%s**",
                usableString, artistPlays, CommandUtil.singlePlural(Math.toIntExact(artistPlays), "plays", "plays"),
                CommandUtil.escapeMarkdown(scrobbledArtist.getArtist())));

    }
}
