package core.commands.abstracts;

import core.commands.Context;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ListCommand<T, Y extends CommandParameters> extends ConcurrentCommand<Y> {

    public ListCommand(ServiceView dao, boolean isLongRunningCommand) {
        super(dao, isLongRunningCommand);
    }

    public ListCommand(ServiceView dao) {
        this(dao, false);
    }

    @Override
    public abstract Parser<Y> initParser();

    @Override
    public void onCommand(Context e, @NotNull Y params) {


        List<T> list = getList(params);
        printList(list, params);
    }

    public abstract List<T> getList(Y params);

    public abstract void printList(List<T> list, Y params);
}
