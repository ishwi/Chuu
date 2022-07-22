package test.state;

import dao.ChuuService;
import dao.entities.RandomUrlEntity;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RandomTest {
    ChuuService dao = new ChuuService(null);

    @Test
    public void name() {
        ChuuService dao = new ChuuService(null);
        Map<RandomUrlEntity, Integer> test = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            RandomUrlEntity randomUrl = dao.getRandomUrl(null);
            test.merge(randomUrl, 1, Integer::sum);
            if (randomUrl == null) {
                System.out.println(randomUrl);
            }


        }
        long count = test.entrySet().stream().filter(x -> x.getKey() == null).count();
        System.out.println(count);
        test.entrySet().stream().filter(x -> x.getKey() != null).sorted(Comparator.comparingInt(randomUrlEntityIntegerEntry -> -randomUrlEntityIntegerEntry.getValue())).forEach((key) -> System.out.println(key.getKey().discordId() + " - " + key.getKey().url() + " - " + key.getValue()));
        for (int i = 0; i < 10; i++) {
            System.out.println();
        }
        List<Map.Entry<Long, Long>> collect = test.entrySet().stream().filter(x -> x.getKey() != null).collect(Collectors.groupingBy(x -> x.getKey().discordId(), Collectors.counting())).entrySet().stream().sorted(Comparator.comparingLong(y -> -y.getValue())).toList();
        collect.forEach(((key) -> {
            System.out.println(key.getKey() + " -  " + key.getValue());
            System.out.flush();
        }));
    }
}
