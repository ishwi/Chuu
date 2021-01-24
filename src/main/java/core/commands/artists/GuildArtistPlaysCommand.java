package core.commands.artists;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

public class GuildArtistPlaysCommand extends ConcurrentCommand<ArtistParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public GuildArtistPlaysCommand(ChuuService dao) {
        super(dao);
        this.spotify = SpotifySingleton.getInstance();
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
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
    public String getName() {
        return "Artist Server Plays";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull ArtistParameters params) throws LastFmException {

        String artist = params.getArtist();

        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotify);

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
        usableString = CommandUtil.cleanMarkdownCharacter(usableString);
        sendMessageQueue(e, String.format("**%s** has **%d** %s on **%s**",
                usableString, artistPlays, CommandUtil.singlePlural(Math.toIntExact(artistPlays), "plays", "plays"),
                CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist())));

    }
}
