package core.parsers.explanation.util;

import org.apache.commons.text.WordUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class MultiExplanation {

    public static List<Explanation> obtainMultiExplanation(String entity, String plural) {
        return obtainMultiExplanation(entity, plural, Collections.emptyList());
    }

    public static List<Explanation> obtainMultiExplanation(String entity, String plural, List<Explanation> explanations) {
        String lowered = entity.toLowerCase();
        String pluralEntity = plural.toLowerCase();
        String title = WordUtils.capitalizeFully(lowered);
        List<Explanation> temp = List.of(() -> new ExplanationLine("%s1 %s2 %s3...".formatted(title, title, title),
                        "You can give any variable number of %ss, if you want to introduce a %s with multiple words you will have to separated them with `-` or `|`".formatted(lowered, lowered)),
                () -> new ExplanationLine("Count",
                        "If you dont give any %s you can also specify a number and it will try to get that number of %s from your recent scrobbles, otherwise just from your last 2 %s".formatted(lowered, pluralEntity, pluralEntity)));
        return Stream.of(temp, explanations).flatMap(Collection::stream).toList();
    }
}

