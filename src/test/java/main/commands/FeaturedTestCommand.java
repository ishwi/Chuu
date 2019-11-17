package main.commands;

import main.commands.utils.CommandTest;
import main.commands.utils.EmbedWithFieldsUtils;
import main.commands.utils.FieldRowMatcher;
import main.commands.utils.TestResources;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FeaturedTestCommand extends CommandTest {
	private static Pattern titlePattern;

	@BeforeClass
	public static void init() {
		titlePattern = Pattern.compile("(.*)'s Featured Artist:");

	}

	@Override
	public String giveCommandName() {
		return "!featured";
	}

	@Override
	public void nullParserReturned() {

	}

	@Test
	public void normalUsage() {
		List<FieldRowMatcher> fieldRowMatchers = new ArrayList<>();
		//Relaxed pattern, wont be the exact one when album or artist has -

		fieldRowMatchers.add(new FieldRowMatcher("Artist:", Pattern.compile("(.*)")));
		fieldRowMatchers.add(new FieldRowMatcher("User:", Pattern.compile("(.*)"),
				matcher ->
						matcher.group(1).equalsIgnoreCase("Chuu") || TestResources.channelWorker.getGuild().getMembers()
								.stream()
								.filter(x -> x.getUser().getName().equals(matcher.group(1))).count() == 1));

		fieldRowMatchers.add(FieldRowMatcher.numberFieldFromRange("Total Artist Plays:", 1));

		EmbedWithFieldsUtils
				.testEmbedWithFields(COMMAND_ALIAS, null, fieldRowMatchers, titlePattern, matcher
						-> TestResources.channelWorker.getGuild().getMembers().stream().filter(x -> matcher.group(1)
						.equals(x.getEffectiveName())).count() == 1);

	}

}
