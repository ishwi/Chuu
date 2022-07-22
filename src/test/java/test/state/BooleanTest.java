package test.state;

import dao.entities.TriFunction;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class BooleanTest {
    @Test
    public void name() {
        Random random = new Random();
        TriFunction<Integer, Boolean, Boolean, Boolean> booleaner = (rank, onlysecond, skipFirst) ->
                onlysecond && rank == 2 || ((!onlysecond) &&
                        skipFirst && rank != 1 ||
                        !onlysecond && !skipFirst);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 1; k < 4; k++) {
                    boolean onlySecond = i % 2 == 0;
                    boolean skipFirst = j % 2 == 0;
                    Boolean apply = booleaner.apply(k, onlySecond, skipFirst);
                    System.out.println(String.format("Rank %d , Only Second %s , SkipFirst %s -> %s", k, onlySecond, skipFirst, apply));
                }
            }
        }
    }
}
