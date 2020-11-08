package core.commands;

import core.exceptions.LastFmException;
import core.imagerenderer.util.PieableListResultWrapper;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.ResultWrapper;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import javax.validation.constraints.NotNull;

public abstract class ResultWrappedCommand<T, Y extends CommandParameters> extends PieableListCommand<ResultWrapper<T>, Y> {
    public PieableListResultWrapper<T, Y> pie;

    ResultWrappedCommand(ChuuService dao) {
        super(dao);
        this.pie = null;
    }

    @Override
    void onCommand(MessageReceivedEvent e, @NotNull Y params) throws LastFmException, InstanceNotFoundException {

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
