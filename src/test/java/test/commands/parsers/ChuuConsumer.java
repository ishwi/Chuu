package test.commands.parsers;

import java.io.IOException;
import java.util.function.Consumer;

public interface ChuuConsumer<T> extends Consumer<T> {


    void doSomething(T o) throws IOException;

    @Override
    default void accept(T t) {
        try {
            doSomething(t);
        } catch (IOException e) {

        }
    }
}
