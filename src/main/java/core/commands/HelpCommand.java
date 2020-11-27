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

import com.google.common.collect.TreeMultimap;
import core.Chuu;
import core.parsers.NoOpParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class HelpCommand extends ConcurrentCommand<CommandParameters> {
    private static final String NO_NAME = "No name provided for this command. Sorry!";
    private static final String NO_DESCRIPTION = "No description has been provided for this command. Sorry!";
    private static final String NO_USAGE = "No usage instructions have been provided for this command. Sorry!";

    private final TreeMultimap<CommandCategory, MyCommand<?>> categoryMap;


    public HelpCommand(ChuuService dao) {
        super(dao);
        categoryMap = TreeMultimap.create(Comparator.comparingInt(CommandCategory::getOrder), (c1, c2) -> {
            String s = c1.getAliases().get(0);
            String s1 = c2.getAliases().get(0);
            return s.compareToIgnoreCase(s1);
        });
        categoryMap.put(this.getCategory(), this);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_INFO;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        NoOpParser noOpParser = new NoOpParser();
        noOpParser.addOptional(new OptionalEntity("all", "DM you a list of all the commands with an explanation"));
        return noOpParser;
    }

    public MyCommand<?> registerCommand(MyCommand<?> command) {
        categoryMap.put(command.getCategory(), command);
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
    public void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) {
        Character prefix = Chuu.getCorrespondingPrefix(e);
        String[] args = commandArgs(e.getMessage());


        if (params.hasOptional("all")) {
            e.getChannel().sendMessage(new MessageBuilder()
                    .append(e.getAuthor())
                    .append(": Help information was sent as a private message.")
                    .mentionUsers(e.getAuthor().getIdLong())
                    .build()).queue();
            e.getAuthor().openPrivateChannel().queue(privateChannel -> sendPrivate(privateChannel, e));
            return;
        }
        if (args.length == 1) {
            sendEmbed(e);
            return;

        }
        doSend(args, e.getChannel(), prefix);
    }

    public void sendPrivate(MessageChannel channel, MessageReceivedEvent e) {
        Character prefix = Chuu.getCorrespondingPrefix(e);
        StringBuilder s = new StringBuilder();
        List<MessageAction> messageActions = new ArrayList<>();
        s.append("A lot of commands accept different time frames which are the following:\n")
                .append(" d: Day \n")
                .append(" w: Week \n")
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

        for (Map.Entry<CommandCategory, Collection<MyCommand<?>>> a : categoryMap.asMap().entrySet()) {
            CommandCategory key = a.getKey();
            Collection<MyCommand<?>> commandList = a.getValue();
            s.append("\n__**").append(key.toString().replaceAll("_", " ")).append(":**__ _").append(key.getDescription()).append("_\n");

            for (MyCommand<?> c : commandList) {
                if (s.length() > 1800) {
                    messageActions.add(channel.sendMessage(new MessageBuilder()
                            .append(s.toString())
                            .build()));
                    s = new StringBuilder();
                }
                String description = c.getDescription();
                description = (description == null || description.isEmpty()) ? NO_DESCRIPTION : description;

                s.append("**").append(prefix).append(c.getAliases().get(0)).append("** - ");
                s.append(description).append("\n");
            }
        }

        messageActions.add(channel.sendMessage(new MessageBuilder()
                .append(s.toString())
                .build()));

        RestAction.allOf(messageActions).queue();
    }

    public void sendEmbed(MessageReceivedEvent e) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        Character correspondingPrefix = Chuu.getCorrespondingPrefix(e);
        for (Map.Entry<CommandCategory, Collection<MyCommand<?>>> a : categoryMap.asMap().entrySet()) {
            StringBuilder s = new StringBuilder();
            CommandCategory key = a.getKey();
            Collection<MyCommand<?>> commandList = a.getValue();
            s.append("\n__**").append(key.toString().replaceAll("_", " ")).append(":**__ _").append(key.getDescription()).append("_\n");
            String collect = commandList.stream().map(x -> "*" + correspondingPrefix + x.getAliases().get(0) + "*").collect(Collectors.joining(", "));
            embedBuilder.addField(new MessageEmbed.Field(s.toString(), collect, false));
        }
        embedBuilder.setFooter(correspondingPrefix + "help \"command\" for the explanation of one command.\n" + correspondingPrefix + "help --all for the whole help message")
                .setTitle("Commands");
        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }

    private void doSend(String[] args, MessageChannel channel, Character prefix) {
        String command = args[1]
                .charAt(0) == prefix ? args[1] : "" + args[1];    //If there is not a preceding . attached to the command we are search, then prepend one.
        for (MyCommand<?> c : categoryMap.values()) {
            if (c.getAliases().contains(command.toLowerCase())) {
                String name = c.getName();
                String description = c.getDescription();
                String usageInstructions = c.getUsageInstructions();

                name = (name == null || name.isEmpty()) ? NO_NAME : name;
                description = (description == null || description.isEmpty()) ? NO_DESCRIPTION : description;
                usageInstructions = (usageInstructions == null || usageInstructions
                        .isEmpty()) ? NO_USAGE : usageInstructions;
                boolean resend = false;
                String realUsageInstructions = usageInstructions;
                String remainingUsageInstructions = null;
                if (realUsageInstructions.length() > 1600) {
                    int i = usageInstructions.substring(0, 1600).lastIndexOf("\n");
                    realUsageInstructions = realUsageInstructions.substring(0, i);
                    remainingUsageInstructions = "-" + usageInstructions.substring(i);
                    resend = true;
                }
                //TODO: Replace with a PrivateMessage
                boolean finalResend = resend;
                String finalRemainingUsageInstructions = remainingUsageInstructions;
                channel.sendMessage(new MessageBuilder().append("**Name:** ").append(name).append("\n")
                        .append("**Description:** ").append(description).append("\n")
                        .append("**Aliases:** ").append(String.valueOf(prefix))
                        .append(String.join(", " + prefix, c.getAliases())).append("\n")
                        .append("**Usage:** ")
                        .append(prefix).append(realUsageInstructions)
                        .build()).queue(x -> {
                    if (finalResend) {
                        channel.sendMessage(finalRemainingUsageInstructions).queue();
                    }
                });
                return;
            }
        }
        channel.sendMessage(new MessageBuilder().append("The provided command '**").append(args[1])
                .append("**' does not exist. Use ").append(prefix).append("help to list all commands.")
                .build()).queue();
    }


}
