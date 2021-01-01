package core.commands.charts;

import core.apis.last.chartentities.UrlCapsule;
import core.exceptions.LastFmException;
import core.imagerenderer.util.IPieableList;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import javax.validation.constraints.NotNull;
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
    protected void onCommand(MessageReceivedEvent e, @NotNull T params) throws LastFmException, InstanceNotFoundException {

        CountWrapper<BlockingQueue<UrlCapsule>> countWrapper = processQueue(params);
        BlockingQueue<UrlCapsule> urlCapsules = countWrapper.getResult();
        if (urlCapsules.isEmpty()) {
            this.noElementsMessage(params);
            return;
        }
        doImage(urlCapsules, params.getX(), params.getY(), params, countWrapper.getRows());
    }


    @Override
    public String configPieChart(PieChart pieChart, T params, int count, String initTitle) {
        return null;
    }

    @Override
    public abstract void noElementsMessage(T parameters);
}
