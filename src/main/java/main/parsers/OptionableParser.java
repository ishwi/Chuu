package main.parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class OptionableParser extends Parser {
	public OptionableParser(OptionalEntity... strings) {
		super();
		opts.addAll(Arrays.asList(strings));
	}

	@Override
	protected void setUpErrorMessages() {
	}

	@Override
	public String[] parseLogic(MessageReceivedEvent e, String[] words) {
		return new String[0];
	}

	@Override
	public String getUsageLogic(String commandName) {

		return "**" + commandName + "**\n";

	}
}
