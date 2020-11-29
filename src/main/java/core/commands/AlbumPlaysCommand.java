package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;


public class AlbumPlaysCommand extends ConcurrentCommand<ArtistAlbumParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public AlbumPlaysCommand(ChuuService dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistAlbumParser(getService(), lastFM);
    }


    @Override
    public String getDescription() {
        return ("How many times you have heard an album!");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("album");
    }

    @Override
    public String getName() {
        return "Get album plays";
    }

    @Override
    public void onCommand(MessageReceivedEvent e, @NotNull ArtistAlbumParameters params) throws LastFmException, InstanceNotFoundException {

        ScrobbledArtist validable = new ScrobbledArtist(params.getArtist(), 0, "");
        CommandUtil.validate(getService(), validable, lastFM, discogsApi, spotify);
        params.setScrobbledArtist(validable);
        doSomethingWithAlbumArtist(validable, params.getAlbum(), e, params.getLastFMData().getDiscordId(), params);

    }

    void doSomethingWithAlbumArtist(ScrobbledArtist artist, String album, MessageReceivedEvent e, long who, ArtistAlbumParameters params) throws LastFmException, InstanceNotFoundException {

        LastFMData data = params.getLastFMData();
        int a = lastFM.getPlaysAlbumArtist(data.getName(), artist.getArtist(), album).getPlays();
        String usernameString = data.getName();

        usernameString = getUserString(e, who, usernameString);

        String ending = a == 1 ? "time " : "times";

        sendMessageQueue(e, "**" + usernameString + "** has listened **" + CommandUtil.cleanMarkdownCharacter(album) + "** " + a + " " + ending);


    }


}
