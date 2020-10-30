package core.commands;

import core.apis.last.chartentities.UrlCapsule;
import core.exceptions.LastFmException;
import core.imagerenderer.util.IPieableList;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.concurrent.BlockingQueue;

public abstract class OnlyChartCommand<T extends ChartParameters> extends ChartableCommand<T> {
    public OnlyChartCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public IPieableList<UrlCapsule, ChartParameters> getPie() {
        return null;
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        T chartParameters = parser.parse(e);
        if (chartParameters == null)
            return;
        CountWrapper<BlockingQueue<UrlCapsule>> countWrapper = processQueue(chartParameters);
        BlockingQueue<UrlCapsule> urlCapsules = countWrapper.getResult();
        if (urlCapsules.isEmpty()) {
            this.noElementsMessage(chartParameters);
            return;
        }
        doImage(urlCapsules, chartParameters.getX(), chartParameters.getY(), chartParameters, countWrapper.getRows());
    }


    @Override
    public String configPieChart(PieChart pieChart, T params, int count, String initTitle) {
        return null;
    }

    @Override
    public abstract void noElementsMessage(T parameters);
}
