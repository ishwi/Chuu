package core.otherlisteners;

import core.Chuu;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class AwaitReady extends ListenerAdapter {
    private final AtomicInteger counter;
    private final Consumer<ShardManager> runnable;

    public AwaitReady(AtomicInteger counter, Consumer<ShardManager> runnable) {
        this.counter = counter;
        this.runnable = runnable;
    }

    public AtomicInteger getCounter() {
        return counter;
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        int i = counter.incrementAndGet();
        if (i == event.getJDA().getShardInfo().getShardTotal()) {
            ShardManager jda = Chuu.getShardManager();
            runnable.accept(jda);
        }
    }
}
