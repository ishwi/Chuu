package core.otherlisteners;

import core.Chuu;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class AwaitReady implements EventListener {
    private final AtomicInteger counter;
    private final Consumer<ShardManager> runnable;
    private final AtomicBoolean inited = new AtomicBoolean(false);

    public AwaitReady(AtomicInteger counter, Consumer<ShardManager> runnable) {
        this.counter = counter;
        this.runnable = runnable;
    }

    public AtomicInteger getCounter() {
        return counter;
    }


    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent e) {
            int i = counter.incrementAndGet();
            if (!inited.get() && i >= event.getJDA().getShardInfo().getShardTotal()) {
                inited.set(true);
                ShardManager jda = Chuu.getShardManager();
                runnable.accept(jda);
            }
        }
    }
}
