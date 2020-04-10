package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ArtistAlbumParser;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;


public class AlbumPlaysCommand extends ConcurrentCommand {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public AlbumPlaysCommand(ChuuService dao) {
        super(dao);
        this.parser = new ArtistAlbumParser(dao, lastFM);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
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
        String[] parsed;
        parsed = parser.parse(e);
        if (parsed == null || parsed.length != 3)
            return;
        String artist = parsed[0];
        String album = parsed[1];
        long whom = Long.parseLong(parsed[2]);
        ScrobbledArtist validable = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(getService(), validable, lastFM, discogsApi, spotify);
        doSomethingWithAlbumArtist(validable, album, e, whom);

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
