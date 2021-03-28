package core.util;

import org.apache.commons.collections4.Bag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

@SuppressWarnings("ALL")
public class UniqueBag<T> implements Bag<T> {
    private final HashMap<T, Integer> inner;

    public UniqueBag() {
        this.inner = new HashMap<>();
    }

    @Override
    @Deprecated
    public int getCount(Object object) {
        throw new UnsupportedOperationException();
    }

    public int getSize(T object) {
        return inner.get(object);
    }

    @Override
    public boolean add(T object) {
        inner.merge(object, 1, Integer::sum);
        return true;
    }

    @Override
    public boolean add(T object, int nCopies) {
        inner.merge(object, nCopies, Integer::sum);
        return false;
    }

    @Override
    public boolean remove(Object object) {
        inner.computeIfPresent((T) object, (t, integer) -> integer - 1 == 0 ? null : integer - 1);
        return true;
    }

    @Override
    public boolean remove(Object object, int nCopies) {
        inner.computeIfPresent((T) object, (t, integer) -> integer - nCopies == 0 ? null : integer - 1);
        return true;
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
        return inner.containsKey(o);
    }

    @Override
    public boolean containsAll(Collection<?> coll) {
        return coll.stream().allMatch(inner::containsKey);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> coll) {
        coll.forEach(this::remove);
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return inner.keySet().iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return inner.keySet().toArray();
    }

    @NotNull
    @Override
    public <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] a) {
        return inner.keySet().toArray(a);
    }
}
