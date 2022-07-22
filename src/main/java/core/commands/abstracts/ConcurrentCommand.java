package core.commands.abstracts;

import core.apis.ExecutorsSingleton;
import core.commands.Context;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;


public abstract class ConcurrentCommand<T extends CommandParameters> extends MyCommand<T> {


    public ConcurrentCommand(ServiceView dao, boolean isLongRunningCommand) {
        super(dao, isLongRunningCommand);
    }

    public ConcurrentCommand(ServiceView dao) {
        this(dao, false);
    }


    @Override
    protected void measureTime(Context e) {
        ExecutorsSingleton.getInstance().execute(() -> super.measureTime(e));
    }
}
