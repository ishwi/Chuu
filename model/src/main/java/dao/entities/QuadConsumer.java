package dao.entities;

@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {
    void apply(A a, B b, C c, D d);

}
