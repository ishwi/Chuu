package dao.entities;

import java.util.function.Function;

public class Memoized<T, J> {
    private final T item;
    private final Function<T, J> toMemoize;
    private J memoized = null;

    public Memoized(T item, Function<T, J> toMemoize) {
        this.item = item;
        this.toMemoize = toMemoize;
    }

    public T getItem() {
        return item;
    }

    public J getMemoized() {
        if (memoized == null) {
            memoized = this.toMemoize.apply(item);
        }
        return memoized;
    }

    @Override
    public String toString() {
        return getMemoized().toString();
    }
}
