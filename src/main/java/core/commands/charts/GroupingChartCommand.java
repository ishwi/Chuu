package core.commands.charts;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.entities.chartentities.TrackDurationChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.last.queues.GroupingQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.exceptions.LastFmException;
import core.parsers.ChartGroupParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartGroupParameters;
import dao.ServiceView;
import dao.entities.ChartMode;
import dao.entities.CountWrapper;
import dao.entities.TimeFrameEnum;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public abstract class GroupingChartCommand extends ChartableCommand<ChartGroupParameters> {
    final DiscogsApi discogsApi;
    final Spotify spotifyApi;

    public GroupingChartCommand(ServiceView dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
    }

    @Override
    public ChartableParser<ChartGroupParameters> initParser() {
        return new ChartGroupParser(db, TimeFrameEnum.WEEK);
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartGroupParameters params) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void onCommand(Context e, @NotNull ChartGroupParameters params) throws LastFmException {

        CountWrapper<GroupingQueue> countWrapper = processGroupedQueue(params);
        if (countWrapper.getResult().isEmpty()) {
            noElementsMessage(params);
            return;
        }
        GroupingQueue queue = countWrapper.getResult();
        List<UrlCapsule> urlCapsules = queue.setUp();

        ChartMode effectiveMode = getEffectiveMode(params);
        if (!(effectiveMode.equals(ChartMode.IMAGE) && params.chartMode().equals(ChartMode.IMAGE))) {
            int sum = urlCapsules.stream().mapToInt(x -> ((TrackDurationChart) x).getSeconds()).sum();
            countWrapper.setRows(sum);
        }
        switch (effectiveMode) {
            case LIST -> doList(urlCapsules, params, countWrapper.getRows());
            case IMAGE_INFO, IMAGE -> doImage(queue, params.getX(), params.getY(), params, countWrapper.getRows());
            case PIE -> doPie(this.pie.doPie(params, urlCapsules), params, countWrapper.getRows());
        }
    }


    public abstract CountWrapper<GroupingQueue> processGroupedQueue(ChartGroupParameters chartParameters) throws LastFmException;


}
