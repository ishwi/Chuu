package core.util.stats;

import java.util.function.BiFunction;

public abstract class Combination<T, J, L, S, W> implements StatsGenerator<T, W> {
    private final StatsGenerator<L, S> other;
    private final BiFunction<J, S, W> otherMapping;

    public Combination(StatsGenerator<L, S> other, BiFunction<J, S, W> b) {
        this.other = other;
        this.otherMapping = b;
    }


    public StatsGenerator<L, S> getOther() {
        return other;
    }

    public BiFunction<J, S, W> getOtherMapping() {
        return otherMapping;
    }

    public abstract StatsGenerator<T, J> leftProcess();
}
