package DAO.Entities;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public
interface TriFunction<A, B, C, R> {

	default <V> TriFunction<A, B, C, V> andThen(
			Function<? super R, ? extends V> after) {
		Objects.requireNonNull(after);
		return (A a, B b, C c) -> after.apply(apply(a, b, c));
	}

	R apply(A a, B b, C c);
}
