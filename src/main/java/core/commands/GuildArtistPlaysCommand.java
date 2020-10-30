package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
    protected CommandCategory getCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<ArtistParameters> getParser() {
        return new ArtistParser(getService(), lastFM);
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ArtistParameters returned = parser.parse(e);
        if (returned == null) {
            return;
        }
        String artist = returned.getArtist();
        long userId = returned.getLastFMData().getDiscordId();

        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotify);

        long artistPlays;
        if (e.isFromGuild()) {
            artistPlays = getService().getServerArtistPlays(e.getGuild().getIdLong(), scrobbledArtist.getArtistId());
        } else {
            LastFMData data = returned.getLastFMData();
            artistPlays = getService().getArtistPlays(scrobbledArtist.getArtistId(), data.getName());
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
