package test.state;

import dao.ChuuService;
import dao.entities.RandomUrlEntity;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class RandomTest {
    ChuuService dao = new ChuuService();

    @Test
    public void name() {
        ChuuService dao = new ChuuService();
        Map<RandomUrlEntity, Integer> test = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            RandomUrlEntity randomUrl = dao.getRandomUrl();
            test.merge(randomUrl, 1, Integer::sum);
            if (randomUrl == null) {
                System.out.println(randomUrl);
            }


        }
        long count = test.entrySet().stream().filter(x -> x.getKey() == null).count();
        System.out.println(count);
        test.entrySet().stream().filter(x -> x.getKey() != null).sorted(Comparator.comparingInt(randomUrlEntityIntegerEntry -> -randomUrlEntityIntegerEntry.getValue())).forEach((key) -> {
            System.out.println(key.getKey().getDiscordId() + " - " + key.getKey().getUrl() + " - " + key.getValue());
        });
        for (int i = 0; i < 10; i++) {
            System.out.println();
        }
        List<Map.Entry<Long, Long>> collect = test.entrySet().stream().filter(x -> x.getKey() != null).collect(Collectors.groupingBy(x -> x.getKey().getDiscordId(), Collectors.counting())).entrySet().stream().sorted(Comparator.comparingLong(y -> -y.getValue()))
                .collect(Collectors.toList());
        collect.forEach(((key) -> {
            System.out.println(key.getKey() + " -  " + key.getValue());
            System.out.flush();
        }));
    }
}
