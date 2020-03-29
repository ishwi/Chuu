package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.queues.GroupingQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.OptionalEntity;
import core.parsers.params.ChartGroupParameters;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public abstract class GroupingChartCommand extends ChartableCommand {
    final DiscogsApi discogsApi;
    final Spotify spotifyApi;

    public GroupingChartCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstanceUsingDoubleLocking();
        this.parser.addOptional(new OptionalEntity("--notime", "dont display time spent"));
    }

    @Override
    public ChartParameters getParameters(String[] message, MessageReceivedEvent e) {
        return new ChartGroupParameters(message, e);
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned;
        returned = parser.parse(e);
        if (returned == null)
            return;
        ChartGroupParameters chartGroupParameters = new ChartGroupParameters(returned, e);
        CountWrapper<GroupingQueue> countWrapper = processGroupedQueue(chartGroupParameters);
        if (countWrapper.getResult().isEmpty()) {
            noElementsMessage(e, chartGroupParameters);
            return;
        }
        GroupingQueue queue = countWrapper.getResult();
        if (chartGroupParameters.isList() || chartGroupParameters.isPieFormat()) {
            List<UrlCapsule> urlCapsules = queue.setUp();
            if (countWrapper.getRows() == -1) {
                countWrapper.setRows(urlCapsules.size());
            }
            if (chartGroupParameters.isPieFormat()) {
                doPie(urlCapsules, chartGroupParameters, countWrapper.getRows());
            } else {
                doList(urlCapsules, chartGroupParameters, countWrapper.getRows());
            }
        } else {
            queue.setUp();
            doImage(queue, chartGroupParameters.getX(), chartGroupParameters.getY(), chartGroupParameters);
        }

    }


    public abstract CountWrapper<GroupingQueue> processGroupedQueue(ChartGroupParameters chartParameters) throws LastFmException;


}
