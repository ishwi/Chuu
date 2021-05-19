package core.commands.abstracts;

import core.apis.ExecutorsSingleton;
import core.commands.Context;
import core.parsers.params.CommandParameters;
import dao.ServiceView;

import java.util.concurrent.ExecutorService;


public abstract class ConcurrentCommand<T extends CommandParameters> extends MyCommand<T> {
    public final ExecutorService executor = ExecutorsSingleton.getInstance();


    public ConcurrentCommand(ServiceView dao, boolean isLongRunningCommand) {
        super(dao, isLongRunningCommand);
    }

    public ConcurrentCommand(ServiceView dao) {
        this(dao, false);
    }


    @Override
    protected void measureTime(Context e) {
        executor.execute(() -> super.measureTime(e));
    }
}
