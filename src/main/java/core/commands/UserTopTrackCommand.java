package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.TrackChart;
import core.apis.last.queues.ArtistQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.ChartParser;
import core.parsers.OptionalEntity;
import core.parsers.params.ChartParameters;
import core.parsers.params.ChartTrackParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class UserTopTrackCommand extends ChartableCommand {

    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;


    public UserTopTrackCommand(ChuuService dao) {
        super(dao);
        parser = new ChartParser(dao);
        parser.replaceOptional("--list", new OptionalEntity("--image", "show this with a chart instead of a list "));
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstanceUsingDoubleLocking();
    }

    @Override
    public ChartParameters getParameters(String[] message, MessageReceivedEvent e) {
        return new ChartTrackParameters(message, e);
    }

    @Override
    public String getDescription() {
        return "Top songs in the provided period";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("toptracks", "tt");
    }

    @Override
    public String getName() {
        return "Top tracks";
    }


    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters params) throws LastFmException {
        ChartTrackParameters params1 = (ChartTrackParameters) params;
        ArtistQueue queue = new ArtistQueue(getService(), discogsApi, spotifyApi, !params1.isList());
        int i = params.makeCommand(lastFM, queue, TopEntity.TRACK, TrackChart.getTrackParser(params));
        return new CountWrapper<>(i, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        return params.initEmbed("'s top tracks", embedBuilder, " has listened to " + count + " tracks");
    }

    @Override
    public void noElementsMessage(MessageReceivedEvent e, ChartParameters parameters) {
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any track%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }
}
