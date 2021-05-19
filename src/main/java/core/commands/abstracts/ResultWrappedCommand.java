package core.commands.abstracts;

import core.commands.Context;
import core.imagerenderer.util.pie.PieableListResultWrapper;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.ResultWrapper;
import org.knowm.xchart.PieChart;

import javax.validation.constraints.NotNull;

public abstract class ResultWrappedCommand<T, Y extends CommandParameters> extends PieableListCommand<ResultWrapper<T>, Y> {
    public PieableListResultWrapper<T, Y> pie;

    protected ResultWrappedCommand(ServiceView dao) {
        super(dao);
        this.pie = null;
    }

    @Override
    protected void onCommand(Context e, @NotNull Y params) {

        if (params.hasOptional("pie")) {
            doPie(getList(params), params);
            return;
        }
        printList(getList(params), params);
    }

    @Override
    public void doPie(ResultWrapper<T> data, Y parameters) {
        PieChart pieChart = this.pie.doPie(parameters, data.getResultList());
        doPie(pieChart, parameters, data.getRows());
    }


    protected abstract String fillPie(PieChart pieChart, Y params, int count);


    public abstract void printList(ResultWrapper<T> list, Y params);

}
