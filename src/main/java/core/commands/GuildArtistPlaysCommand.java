package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class GuildArtistPlaysCommand extends ConcurrentCommand {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public GuildArtistPlaysCommand(ChuuService dao) {
        super(dao);
        this.parser = new ArtistParser(dao, lastFM);
        this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
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
        String[] returned = parser.parse(e);
        if (returned == null) {
            return;
        }
        String artist = returned[0];
        long whom = Long.parseLong(returned[1]);

        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotify);
        long artistPlays;
        if (e.isFromGuild()) {
            LastFMData data = getService().findLastFMData(whom);
            artistPlays = getService().getArtistPlays(scrobbledArtist.getArtistId(), data.getName());
        } else {
            artistPlays = getService().getServerArtistPlays(e.getGuild().getIdLong(), scrobbledArtist.getArtistId());
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
