/**
 * Copyright 2015-2016 Austin Keener
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package main.Commands;

import main.Exceptions.ParseException;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class HelpCommand extends MyCommand {
	private static final String NO_NAME = "No name provided for this command. Sorry!";
	private static final String NO_DESCRIPTION = "No description has been provided for this command. Sorry!";
	private static final String NO_USAGE = "No usage instructions have been provided for this command. Sorry!";

	private TreeMap<String, MyCommand> commands;

	public HelpCommand() {

		commands = new TreeMap<>();
	}

	public MyCommand registerCommand(MyCommand command) {
		commands.put(command.getAliases().get(0), command);
		return command;
	}

	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {
		if (!e.isFromType(ChannelType.PRIVATE)) {
			e.getTextChannel().sendMessage(new MessageBuilder()
					.append(e.getAuthor())
					.append(": Help information was sent as a private message.")
					.build()).queue();
		}
		sendPrivate(e.getAuthor().openPrivateChannel().complete(), args);
	}


	@Override
	public List<String> getAliases() {
		return Arrays.asList("!help", "!commands");
	}

	@Override
	public String getDescription() {
		return "Command that helps use all other commands!";
	}

	@Override
	public String getName() {
		return "Help Command";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList(
				".help   **OR**  .help *<command>*\n"
						+ ".help - returns the list of commands along with a simple description of each.\n"
						+ ".help <command> - returns the name, description, aliases and usage information of a command.\n"
						+ "   - This can use the aliases of a command as input as well.\n"
						+ "__Example:__ .help ann");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		return new String[0];
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {

	}

	private void sendPrivate(PrivateChannel channel, String[] args) {
		if (args.length < 2) {
			StringBuilder s = new StringBuilder();
			for (MyCommand c : commands.values()) {
				String description = c.getDescription();
				description = (description == null || description.isEmpty()) ? NO_DESCRIPTION : description;

				s.append("**").append(c.getAliases().get(0)).append("** - ");
				s.append(description).append("\n");
			}

			channel.sendMessage(new MessageBuilder()
					.append("The following commands are supported by the bot\n")
					.append(s.toString())
					.build()).queue();
		} else {
			String command = args[1].charAt(0) == '!' ? args[1] : "!" + args[1];    //If there is not a preceding . attached to the command we are search, then prepend one.
			for (MyCommand c : commands.values()) {
				if (c.getAliases().contains(command)) {
					String name = c.getName();
					String description = c.getDescription();
					List<String> usageInstructions = c.getUsageInstructions();
					name = (name == null || name.isEmpty()) ? NO_NAME : name;
					description = (description == null || description.isEmpty()) ? NO_DESCRIPTION : description;
					usageInstructions = (usageInstructions == null || usageInstructions.isEmpty()) ? Collections.singletonList(NO_USAGE) : usageInstructions;

					//TODO: Replace with a PrivateMessage
					channel.sendMessage(new MessageBuilder().append("**Name:** ").append(name).append("\n").append("**Description:** ").append(description).append("\n").append("**Alliases:** ").append(String.join(", ", c.getAliases())).append("\n")
							.append("**Usage:** ")
							.append(usageInstructions.get(0))
							.build()).queue();
					for (int i = 1; i < usageInstructions.size(); i++) {
						channel.sendMessage(new MessageBuilder().append("__").append(name).append(" Usage Cont. (").append(String.valueOf(i + 1)).append(")__\n")
								.append(usageInstructions.get(i))
								.build()).queue();
					}
					return;
				}
			}
			channel.sendMessage(new MessageBuilder().append("The provided command '**").append(args[1]).append("**' does not exist. Use .help to list all commands.")
					.build()).queue();
		}
	}
}