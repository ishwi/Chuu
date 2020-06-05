package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.chartentities.TrackDurationChart;
import core.apis.last.queues.GroupingQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ChartGroupParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartGroupParameters;
import dao.ChuuService;
import dao.entities.ChartMode;
import dao.entities.CountWrapper;
import dao.entities.TimeFrameEnum;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public abstract class GroupingChartCommand extends ChartableCommand<ChartGroupParameters> {
    final DiscogsApi discogsApi;
    final Spotify spotifyApi;

    public GroupingChartCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
    }

    @Override
    public ChartableParser<ChartGroupParameters> getParser() {
        return new ChartGroupParser(getService(), TimeFrameEnum.WEEK);
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartGroupParameters params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ChartGroupParameters chartGroupParameters = parser.parse(e);
        if (chartGroupParameters == null) {
            return;
        }
        CountWrapper<GroupingQueue> countWrapper = processGroupedQueue(chartGroupParameters);
        if (countWrapper.getResult().isEmpty()) {
            noElementsMessage(chartGroupParameters);
            return;
        }
        GroupingQueue queue = countWrapper.getResult();
        List<UrlCapsule> urlCapsules = queue.setUp();

        ChartMode effectiveMode = getEffectiveMode(chartGroupParameters);
        if (!(effectiveMode.equals(ChartMode.IMAGE) && chartGroupParameters.chartMode().equals(ChartMode.IMAGE))) {
            int sum = urlCapsules.stream().mapToInt(x -> ((TrackDurationChart) x).getSeconds()).sum();
            countWrapper.setRows(sum);
        }
        switch (effectiveMode) {
         
            case LIST:
                doList(urlCapsules, chartGroupParameters, countWrapper.getRows());
                return;
            case IMAGE_INFO:
            case IMAGE:
                doImage(queue, chartGroupParameters.getX(), chartGroupParameters.getY(), chartGroupParameters, countWrapper.getRows());
                return;
            case PIE:
                PieChart pieChart = this.pie.doPie(chartGroupParameters, urlCapsules);
                doPie(pieChart, chartGroupParameters, countWrapper.getRows());
        }
    }


    public abstract CountWrapper<GroupingQueue> processGroupedQueue(ChartGroupParameters chartParameters) throws LastFmException;


}
