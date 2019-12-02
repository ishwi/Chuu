package main.commands.utils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmbedWithFieldsUtils {

	public static void testEmbedWithFields(String command, @Nullable Pattern noEmbedCase, List<FieldRowMatcher> fieldRowMatchers, Pattern title, Predicate<Matcher> titlePredicate) {

		new EmbedTesterBuilder(command)
				.titlePattern(title)
				.titleMatch(titlePredicate)
				.noEmbbed(noEmbedCase)
				.fieldRowMatch(fieldRowMatchers)
				.build().GeneralFunction();

	}

}
