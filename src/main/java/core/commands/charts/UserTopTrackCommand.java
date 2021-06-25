package core.commands.charts;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.last.queues.ArtistQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartParameters;
import core.parsers.utils.Optionals;
import dao.ServiceView;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class UserTopTrackCommand extends ChartableCommand<ChartParameters> {

    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;


    public UserTopTrackCommand(ServiceView dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public ChartableParser<ChartParameters> initParser() {
        ChartParser chartParser = new ChartParser(db);
        chartParser.replaceOptional("list", Optionals.IMAGE.opt);
        chartParser.addOptional(Optionals.LIST.opt.withBlockedBy("image", "pie"));
        chartParser.setExpensiveSearch(false);
        return chartParser;
    }

    @Override
    public String getSlashName() {
        return "toptracks";
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
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters param) throws LastFmException {
        ArtistQueue queue = new ArtistQueue(db, discogsApi, spotifyApi, !param.isList());
        int i = param.makeCommand(lastFM, queue, TopEntity.TRACK, ChartUtil.getParser(param.getTimeFrameEnum(), TopEntity.TRACK, param, lastFM, param.getUser()));
        return new CountWrapper<>(i, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        String handleCount;
        if (!params.getTimeFrameEnum().isNormal()) {
            handleCount = "'s top " + count + " tracks";
        } else {
            handleCount = " has listened to " + count + " tracks";
        }
        return params.initEmbed("'s top tracks", embedBuilder, handleCount, params.getUser().getName());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s top tracks" + time);
        if (!params.getTimeFrameEnum().isNormal()) {
            return String.format("%s top %d tracks%s", initTitle, count, time);
        } else {
            return String.format("%s has listened to %d songs%s (showing top %d)", initTitle, count, time, params.getX() * params.getY());
        }
    }


    @Override
    public void noElementsMessage(ChartParameters parameters) {
        Context e = parameters.getE();

        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any track%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }

}
