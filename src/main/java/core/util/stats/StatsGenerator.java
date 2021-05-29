package core.util.stats;

import java.util.function.BiFunction;


public interface StatsGenerator<T, J> {

    //  J = List<SA> ||  S = UserInfo
    // W = Pair<J,S>
    //
    default <W, L, S> StatsGenerator<T, W> or(StatsGenerator<T, S> other, BiFunction<J, S, W> mapper) {
        return new Combination<>(other, mapper) {

            @Override
            public W process(T object) {
                J j = leftProcess().process(object);
                S s = other.process(object);
                return mapper.apply(j, s);
            }

            @Override
            public StatsGenerator<T, J> leftProcess() {
                return StatsGenerator.this;
            }
        };
    }


    J process(T object);

}
