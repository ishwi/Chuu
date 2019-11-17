package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.EmbedWithFieldsUtils;
import main.commands.utils.FieldRowMatcher;
import main.commands.utils.TestResources;
import net.dv8tion.jda.api.entities.Member;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RecentCommandTest extends CommandTest {
	private static Pattern titlePattern;

	@BeforeClass
	public static void init() {
		titlePattern = Pattern.compile("(.*)'s last 5 tracks");

	}

	@Override
	public String giveCommandName() {
		return "!recent";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.onlyUsernameParser(COMMAND_ALIAS);
	}

	@Test
	public void normalUsage() {

		//Usually the help message is sent to a private channel but i didnt find a way to get a private message from a marker so i opted to
		//send the message directly to the test channel

		List<FieldRowMatcher> fieldRowMatchers = new ArrayList<>();
		//Relaxed pattern, wont be the exact one when album or artist has -
		Pattern compile = Pattern.compile("\\*\\*(.*?)\\*\\* - (.*?) \\| (.*)");

		for (int i = 1; i <= 5; i++) {
			fieldRowMatchers.add(new FieldRowMatcher("Track #" + i + ":", compile));
		}

		EmbedWithFieldsUtils
				.testEmbedWithFields("!recent", null, fieldRowMatchers, titlePattern, matcher -> matcher.group(1)
						.equalsIgnoreCase(TestResources.testerJdaUsername));
		Member memberById = TestResources.channelWorker.getGuild()
				.getMemberById(TestResources.ogJDA.getSelfUser().getId());
		assert memberById != null;
		String effectiveName = memberById.getEffectiveName();
		EmbedWithFieldsUtils
				.testEmbedWithFields("!recent " + TestResources.ogJDA.getSelfUser()
						.getAsMention(), null, fieldRowMatchers, titlePattern, matcher -> matcher.group(1)
						.equalsIgnoreCase(effectiveName));
	}


}
