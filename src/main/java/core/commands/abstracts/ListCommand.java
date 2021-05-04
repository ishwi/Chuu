package core.commands.abstracts;

import core.commands.Context;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;

import javax.validation.constraints.NotNull;
import java.util.List;

public abstract class ListCommand<T, Y extends CommandParameters> extends ConcurrentCommand<Y> {


    public ListCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public abstract Parser<Y> initParser();

    @Override
    protected void onCommand(Context e, @NotNull Y params) {


        List<T> list = getList(params);
        printList(list, params);
    }

    public abstract List<T> getList(Y params);

    public abstract void printList(List<T> list, Y params);
}
