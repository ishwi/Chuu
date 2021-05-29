package core.util.stats;

public interface StatsProcesser<T> extends StatsGenerator<T, String> {

    String process(T object);
}
