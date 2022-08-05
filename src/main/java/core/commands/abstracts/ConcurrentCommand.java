package core.commands.abstracts;

import core.apis.ExecutorsSingleton;
import core.commands.Context;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;


public abstract class ConcurrentCommand<T extends CommandParameters> extends MyCommand<T> {

    public static final Set<ThreadStats> threadStats = new LinkedHashSet<>();
    public static final ExecutorService ex = ExecutorsSingleton.getInstance();

    public ConcurrentCommand(ServiceView dao, boolean isLongRunningCommand) {
        super(dao, isLongRunningCommand);
    }

    public ConcurrentCommand(ServiceView dao) {
        this(dao, false);
    }


    @Override
    protected void measureTime(Context e) {
        ex.execute(() -> {
            ThreadStats stats = new ThreadStats(Thread.currentThread(), this.getAliases().get(0), e.toLog());
            try {
                threadStats.add(stats);
                super.measureTime(e);
            } finally {
                threadStats.remove(stats);
            }
        });
    }

    public record ThreadStats(Thread threadName, String command, String e) {
        @Override
        public String toString() {
            return "**%s** | **%s** | *%s*\n%s".formatted(threadName, command, e, Arrays.stream(threadName.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n\t")));
        }
    }
}
