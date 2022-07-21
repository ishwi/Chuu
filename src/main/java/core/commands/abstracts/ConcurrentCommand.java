package core.commands.abstracts;

import core.commands.Context;
import core.parsers.params.CommandParameters;
import core.util.ChuuVirtualPool;
import core.util.ServiceView;

import java.util.concurrent.ExecutorService;


public abstract class ConcurrentCommand<T extends CommandParameters> extends MyCommand<T> {
    public static final ExecutorService executor = ChuuVirtualPool.of("Commands");


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
