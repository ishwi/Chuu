package core.commands.charts;

import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.Context;
import core.exceptions.LastFmException;
import core.imagerenderer.util.pie.IPieableList;
import core.parsers.params.ChartParameters;
import dao.ServiceView;
import dao.entities.CountWrapper;
import org.knowm.xchart.PieChart;

import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;

public abstract class OnlyChartCommand<T extends ChartParameters> extends ChartableCommand<T> {
    public OnlyChartCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    public IPieableList<UrlCapsule, ChartParameters> getPie() {
        return null;
    }

    @Override
    protected void onCommand(Context e, @Nonnull T params) throws LastFmException {

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
