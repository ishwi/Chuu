package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.imagerenderer.UrlCapsuleConcurrentQueue;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class ArtistCommand extends ChartCommand {
    final DiscogsApi discogsApi;
    final Spotify spotifyApi;


    public ArtistCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstanceUsingDoubleLocking();
    }

    @Override
    public String getDescription() {
        return "Returns a chart with artist images";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("artchart", "charta", "artistchart", "ca");
    }

    @Override
    public void processQueue(String username, String time, int x, int y, MessageReceivedEvent e, boolean writeTitles, boolean writePlays) throws LastFmException {
        UrlCapsuleConcurrentQueue queue = new UrlCapsuleConcurrentQueue(getService(), discogsApi, spotifyApi);
        lastFM.getUserList(username, time, x, y, false, queue);
        generateImage(queue, x, y, e, writeTitles, writePlays);
    }

    @Override
    public String getName() {
        return "Artist Chart";
    }


}
