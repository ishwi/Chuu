package main.commands.utils;

import org.junit.ClassRule;

public abstract class CommandTest {
	@ClassRule
	public static final TestResources res = new TestResources();
	public final String COMMAND_ALIAS;

	public CommandTest() {
		COMMAND_ALIAS = giveCommandName();
	}

	public abstract String giveCommandName();

	public abstract void nullParserReturned();


}
