package core.parsers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class PrefixParser extends Parser {
	private static final List<Character> acceptecChars = (Arrays
			.asList('!', '@', '#', '$', '%', '^', '_', '.', ',', ';', ':', '~'));

	@Override
	protected void setUpErrorMessages() {
		errorMessages.put(0, "Pls only intruduce the prefix you want the bot to use");
		StringBuilder s = new StringBuilder();
		acceptecChars.forEach(s::append);
		errorMessages.put(1, "The prefix must be one of the following: " + s.toString());
		errorMessages.put(2, "Insufficient Permissions, only a mod  can");

	}

	@Override
	protected String[] parseLogic(MessageReceivedEvent e, String[] words) {
		if (e.getMember() == null || !e.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			sendError(getErrorMessage(2), e);
			return null;
		}
		if (words.length != 1) {
			sendError(this.getErrorMessage(0), e);
			return null;
		}
		String expectedChar = words[0];
		if (expectedChar.length() != 1 || !acceptecChars.contains(expectedChar.charAt(0))) {
			sendError(this.getErrorMessage(1), e);
			return null;
		}
		return new String[]{expectedChar};
	}

	@Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *[!@#$%^_.,;:~]* " +
               "\n\tAn elevated user should use  one of the above symbols";
    }
}
