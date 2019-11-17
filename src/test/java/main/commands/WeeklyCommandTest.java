package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.EmbedUtils;
import org.junit.Test;

import java.util.regex.Pattern;

public class WeeklyCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!weekly";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.onlyUsernameParser(COMMAND_ALIAS);
	}

	@Test
	public void normalWorkingTest() {

		Pattern pattern = Pattern
				.compile("(Mon|Tues|Wed(nes)?|Thur(s)?|Fri|Sat(ur)?|Sun)(day)?, \\d{2}/\\d{2}: \\d+ minutes, \\(\\d+:\\d+h\\) on \\d+ track(:?s)?");
		EmbedUtils.testEmbed(COMMAND_ALIAS, pattern, null, "${header}'s week", false, false, null, null);
	}
}
