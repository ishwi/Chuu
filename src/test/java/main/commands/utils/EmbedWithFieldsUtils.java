package main.commands.utils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.Assert;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.commands.utils.TestResources.channelWorker;
import static org.awaitility.Awaitility.await;

public class EmbedWithFieldsUtils {

	public static void testEmbedWithFields(String command, @Nullable Pattern noEmbedCase, List<FieldRowMatcher> fieldRowMatchers, Pattern title, Predicate<Matcher> titlePredicate) {
		long id = channelWorker.sendMessage(command).complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});

		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		if (message.getEmbeds().isEmpty()) {
			if (noEmbedCase != null) {
				Matcher matcher = noEmbedCase.matcher(message.getContentStripped());
				Assert.assertTrue(matcher.matches());
			}
			return;
		}
		MessageEmbed messageEmbed = message.getEmbeds().get(0);

		FieldMatcher internalMatcher = (field, string, pattern, matcherPredicate) -> {
			if (field != null && field.getName() != null && field.getName().equals(string)) {
				Matcher matcher = pattern.matcher(field.getValue());
				return (matcher.matches()) && matcherPredicate.test(matcher);
			}
			return false;
		};
		List<FieldRowMatcher> localFieldRowMatcher = new ArrayList<>(fieldRowMatchers);
		List<MessageEmbed.Field> fields = new ArrayList<>(messageEmbed.getFields());
		for (int i = 0; i < fields.size(); i++) {
			for (int j = 0; j < localFieldRowMatcher.size(); j++) {
				FieldRowMatcher fieldRowMatcher = localFieldRowMatcher.get(j);
				if (internalMatcher
						.apply(fields.get(i), fieldRowMatcher.getTitle(), fieldRowMatcher.getPattern(), fieldRowMatcher
								.getPredicate())) {
					fields.remove(i--);
					localFieldRowMatcher.remove(j);
					break;
				}
			}


		}
		Assert.assertEquals(0, fields.size());
		Assert.assertEquals(0, localFieldRowMatcher.size());
	}

}
