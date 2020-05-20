package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistAlbumParameters> getParser() {
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
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ArtistAlbumParameters parsed = parser.parse(e);
        if (parsed == null)
            return;
        ScrobbledArtist validable = new ScrobbledArtist(parsed.getArtist(), 0, "");
        CommandUtil.validate(getService(), validable, lastFM, discogsApi, spotify);
        parsed.setScrobbledArtist(validable);
        doSomethingWithAlbumArtist(validable, parsed.getAlbum(), e, parsed.getUser().getIdLong());

    }

    void doSomethingWithAlbumArtist(ScrobbledArtist artist, String album, MessageReceivedEvent e, long who) throws InstanceNotFoundException, LastFmException {

        LastFMData data = getService().findLastFMData(who);

        int a = lastFM.getPlaysAlbumArtist(data.getName(), artist.getArtist(), album).getPlays();
        String usernameString = data.getName();

        usernameString = getUserString(e, who, usernameString);

        String ending = a == 1 ? "time " : "times";

        sendMessageQueue(e, "**" + usernameString + "** has listened **" + CommandUtil.cleanMarkdownCharacter(album) + "** " + a + " " + ending);


    }


}
