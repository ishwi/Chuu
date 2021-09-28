import org.apache.commons.collections4.Bag;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

class UniqueBag<T> implements Bag<T> {
    private final ConcurrentHashMap<T, Integer> inner;
    private final Class<T> clazz;


    public UniqueBag(Class<T> clazz) {
        this.clazz = clazz;
        this.inner = new ConcurrentHashMap<>();
    }

    private T getT(Object object) {
        if (clazz.isAssignableFrom(object.getClass())) {
            return clazz.cast(object);
        }
        throw new ClassCastException();
    }


    public int getSize(T object) {
        return inner.get(object);
    }

    @Override
    public int getCount(Object object) {
        T t = getT(object);
        return inner.getOrDefault(t, 0);
    }

    @Override
    public boolean add(T object) {
        inner.merge(object, 1, Integer::sum);
        return true;
    }

    @Override
    public boolean add(T object, int nCopies) {
        inner.merge(object, nCopies, Integer::sum);
        return true;
    }

    @Override
    public boolean remove(Object object) {
        T item = getT(object);
        return inner.computeIfPresent(item, (t, integer) -> integer - 1 == 0 ? null : integer - 1) == null;
    }

    @Override
    public boolean remove(Object object, int nCopies) {
        T item = getT(object);
        return inner.computeIfPresent(item, (t, integer) -> integer - nCopies == 0 ? null : integer - nCopies) == null;
    }


    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        return c.stream().map(this::getT).collect(
                Collectors.collectingAndThen(
                        Collectors.groupingBy(Function.identity(), Collectors.counting()),
                        result -> result.entrySet().stream().allMatch((k) -> this.inner.get(k.getKey()) == Math.toIntExact(k.getValue()))
                )
        );
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends T> c) {
        c.forEach(this::add);
        return c.size() > 0;
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {
        int size = c.size();
        c.forEach(this::remove);
        return c.size() != size;

    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        return this.inner.keySet().retainAll(c);
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public Set<T> uniqueSet() {
        return inner.keySet();
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return inner.containsKey(getT(o));
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return inner.keySet().iterator();
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        return this.inner.keySet().toArray();
    }

    @Nonnull
    @Override
    public <T1> T1[] toArray(@Nonnull T1[] a) {
        throw new UnsupportedOperationException();
    }


}
