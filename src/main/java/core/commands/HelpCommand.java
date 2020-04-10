/*
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
package core.commands;

import core.Chuu;
import dao.ChuuService;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class HelpCommand extends ConcurrentCommand {
    private static final String NO_NAME = "No name provided for this command. Sorry!";
    private static final String NO_DESCRIPTION = "No description has been provided for this command. Sorry!";
    private static final String NO_USAGE = "No usage instructions have been provided for this command. Sorry!";

    private final TreeMap<String, MyCommand> commands;

    public HelpCommand(ChuuService dao) {
        super(dao);

        commands = new TreeMap<>();
        commands.put(this.getAliases().get(0), this);
    }

    public MyCommand registerCommand(MyCommand command) {
        commands.put(command.getAliases().get(0), command);
        return command;
    }

    @Override
    public String getDescription() {
        return "Command that helps to use all other commands!";
    }

    @Override
    public String getUsageInstructions() {
        return
                "help   **OR**  help *<command>*\n"
                + "help - returns the list of commands along with a simple description of each.\n"
                + "help <command> - returns the name, description, aliases and usage information of a command.\n"
                + "   - This can use the aliases of a command as input as well.\n"
                + "Example: !help chart";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("help", "commands");
    }

    @Override
    public String getName() {
        return "Help Command";
    }

    @Override
    public void onCommand(MessageReceivedEvent e) {
        Character prefix = Chuu.getCorrespondingPrefix(e);
        String[] args = commandArgs(e.getMessage());

        if (!e.isFromType(ChannelType.PRIVATE)) {
            if (args.length < 2) {
                e.getTextChannel().sendMessage(new MessageBuilder()
                        .append(e.getAuthor())
                        .append(": Help information was sent as a private message.")
                        .build()).queue();
            } else {
                doSend(args, e.getChannel(), prefix);
                return;
            }

        }
        sendPrivate(e.getAuthor().openPrivateChannel().complete(), args, prefix);
    }

    public void sendPrivate(MessageChannel channel, String[] args, Character prefix) {
        if (args.length < 2) {
            StringBuilder s = new StringBuilder();
            s.append("A lot of commands accept different time frames which are the following:\n" +
                     " w: Week \n")
                    .append(" m: Month \n")
                    .append(" q: quarter \n")
                    .append(" s: semester \n")
                    .append(" y: year \n")
                    .append(" a: alltime \n")
                    .append("\n")
                    .append("You can use ").append(prefix).append(getAliases().get(0))
                    .append(" + other command to get a exact description of what a command accepts\n")
                    .append("\n")
                    .append("The following commands are supported by the bot\n");

            for (MyCommand c : commands.values()) {

                if (s.length() > 1800) {
                    channel.sendMessage(new MessageBuilder()
                            .append(s.toString())
                            .build()).complete();
                    s = new StringBuilder();
                }
                String description = c.getDescription();
                description = (description == null || description.isEmpty()) ? NO_DESCRIPTION : description;

                s.append("**").append(prefix).append(c.getAliases().get(0)).append("** - ");
                s.append(description).append("\n");
            }

            channel.sendMessage(new MessageBuilder()
                    .append(s.toString())
                    .build()).complete();
        } else {
            doSend(args, channel, prefix);
        }
    }

    private void doSend(String[] args, MessageChannel channel, Character prefix) {
        String command = args[1]
                                 .charAt(0) == prefix ? args[1] : "" + args[1];    //If there is not a preceding . attached to the command we are search, then prepend one.
        for (MyCommand c : commands.values()) {
            if (c.getAliases().contains(command)) {
                String name = c.getName();
                String description = c.getDescription();
                String usageInstructions = c.getUsageInstructions();

                name = (name == null || name.isEmpty()) ? NO_NAME : name;
                description = (description == null || description.isEmpty()) ? NO_DESCRIPTION : description;
                usageInstructions = (usageInstructions == null || usageInstructions
                        .isEmpty()) ? NO_USAGE : usageInstructions;

                //TODO: Replace with a PrivateMessage
                channel.sendMessage(new MessageBuilder().append("**Name:** ").append(name).append("\n")
                        .append("**Description:** ").append(description).append("\n")
                        .append("**Alliases:** ").append(String.valueOf(prefix))
                        .append(String.join(", " + prefix, c.getAliases())).append("\n")
                        .append("**Usage:** ")
                        .append(prefix).append(usageInstructions)
                        .build()).queue();
                return;
            }
        }
        channel.sendMessage(new MessageBuilder().append("The provided command '**").append(args[1])
                .append("**' does not exist. Use ").append(prefix).append("help to list all commands.")
                .build()).queue();
    }


}