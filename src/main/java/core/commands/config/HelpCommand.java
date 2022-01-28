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
package core.commands.config;

import com.google.common.collect.Lists;
import core.Chuu;
import core.apis.lyrics.TextSplitter;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.abstracts.MyCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.PrivacyUtils;
import core.otherlisteners.SelectionEventListener;
import core.parsers.HelpParser;
import core.parsers.Parser;
import core.parsers.params.WordParameter;
import core.parsers.utils.OptionalEntity;
import dao.ServiceView;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HelpCommand extends ConcurrentCommand<WordParameter> {
    private static final String NO_NAME = "No name provided for this command. Sorry!";
    private static final String NO_DESCRIPTION = "No description has been provided for this command. Sorry!";
    private static final String NO_USAGE = "No usage instructions have been provided for this command. Sorry!";

    private final TreeMap<CommandCategory, SortedSet<MyCommand<?>>> categoryMap;
    private final Comparator<MyCommand<?>> myCommandComparator = (c1, c2) -> {
        String s = c1.getAliases().get(0);
        String s1 = c2.getAliases().get(0);
        return s.compareToIgnoreCase(s1);
    };
    private static final String CUSTOM_ALL = "custom_all";


    public HelpCommand(ServiceView dao) {
        super(dao);
        categoryMap = new TreeMap<>(Comparator.comparingInt(CommandCategory::getOrder));
        registerCommand(this);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.STARTING;
    }

    @Override
    public Parser<WordParameter> initParser() {
        return new HelpParser(new OptionalEntity("all", "DM you a list of all the commands with an explanation"),
                new OptionalEntity("complete", "show all the commands on a huge embed"));
    }

    public MyCommand<?> registerCommand(MyCommand<?> command) {
        SortedSet<MyCommand<?>> myCommands = categoryMap.get(command.getCategory());
        if (myCommands == null) {
            TreeSet<MyCommand<?>> set = new TreeSet<>(myCommandComparator);
            set.add(command);
            categoryMap.put(command.getCategory(), set);
        } else {
            myCommands.add(command);
        }
        return command;
    }

    @Override
    public String getDescription() {
        return "Command that helps to use all other commands!";
    }

    @Override
    public String getUsageInstructions() {
        return
                """
                        help   **OR**  help *<command>*
                        help - returns the list of commands along with a simple description of each.
                        help <command> - returns the name, description, aliases and usage information of a command.
                           - This can use the aliases of a command as input as well.
                        Example: !help chart""";
    }

    @Override
    public List<String> getAliases() {
        return List.of("help", "h");
    }

    @Override
    public String getName() {
        return "Help Command";
    }

    @Override
    public void onCommand(Context e, @Nonnull WordParameter params) {
        Character prefix = e.getPrefix();
        if (params.hasOptional("all")) {

            e.sendMessage(new MessageBuilder()
                            .append(e.getAuthor())
                            .append(": Help information was sent as a private message.")
                            .build(), e.getAuthor())
                    .flatMap(z -> e.getAuthor().openPrivateChannel())
                    .queue(privateChannel -> sendPrivate(privateChannel, e));
            return;
        }
        if (params.getWord() == null) {
            if (params.hasOptional("complete")) {
                sendEmbed(e);
            } else {
                sendCategories(e);
            }
            return;

        }
        doSend(params.getWord(), e, prefix);
    }

    private List<ActionRow> invalidateAll(List<ActionRow> rows) {
        List<ActionRow> disabled = new ArrayList<>();
        for (ActionRow row : rows) {
            List<ItemComponent> rowComponentes = new ArrayList<>();
            for (ItemComponent component : row.getComponents()) {
                if (component instanceof SelectMenu menu) {
                    rowComponentes.add(menu.asDisabled());
                } else if (component instanceof Button button) {
                    rowComponentes.add(button.asDisabled());
                } else {
                    rowComponentes.add(component);
                }
            }
            disabled.add(ActionRow.of(rowComponentes));
        }
        return disabled;
    }

    public void sendCategories(Context e) {
        List<CommandCategory> categories = categoryMap.keySet().stream().sorted(Comparator.comparingInt(CommandCategory::getOrder)).toList();
        SelfUser su = e.getJDA().getSelfUser();
        EmbedBuilder eb = new ChuuEmbedBuilder(e).setAuthor("%s commands".formatted(su.getName()), PrivacyUtils.getLastFmUser(Chuu.DEFAULT_LASTFM_ID), su.getAvatarUrl());


        ActionRow category = buildMenuCategory(categories, CommandCategory.STARTING.name());
        List<ActionRow> commandRow = buildMenuCommand(eb, e.getPrefix(), CommandCategory.STARTING, null);

        String footer = ("%shelp --complete for the full help message%n" +
                "%shelp --all for a dm with more info%n").formatted(e.getPrefix(), e.getPrefix());
        eb.setFooter(footer);

        List<ActionRow> rows = Stream.concat(Stream.of(category), commandRow.stream()).toList();
        e.sendMessage(eb.build(), rows).queue(message ->

                new SelectionEventListener(eb, message, true, 120, (embedBuilder, actionRows) ->
                {
                    embedBuilder.setFooter(footer);
                    return new SelectionEventListener.SelectionResponse(embedBuilder, invalidateAll(actionRows));
                }, e.getAuthor().getIdLong(), e,
                        this::doAction
                ));

    }

    private SelectionEventListener.SelectionResponse doAction(Context e, SelectMenu SelectMenu, List<String> options, EmbedBuilder eb, List<ActionRow> rows) {

        String selected = options.get(0);
        String id = SelectMenu.getId();
        eb.clearFields();
        eb.setFooter(null);

        assert id != null;
        // First selector pressed
        if (id.equals("category_id")) {
            List<CommandCategory> categories = categoryMap.keySet().stream().sorted(Comparator.comparingInt(CommandCategory::getOrder)).toList();
            if (Objects.equals(selected, CUSTOM_ALL)) {
                for (Map.Entry<CommandCategory, SortedSet<MyCommand<?>>> a : categoryMap.entrySet()) {
                    buildCategory(eb, e.getPrefix(), a.getKey(), a.getValue());
                }
                ActionRow categoriesRow = buildMenuCategory(categories, CUSTOM_ALL);
                return new SelectionEventListener.SelectionResponse(eb, List.of(categoriesRow));
            } else {
                CommandCategory commandCategory = CommandCategory.valueOf(selected);
                ActionRow categoriesRow = buildMenuCategory(categories, commandCategory.name());
                List<ActionRow> commands = buildMenuCommand(eb, e.getPrefix(), commandCategory, null);
                List<ActionRow> row = Stream.concat(Stream.of(categoriesRow), commands.stream()).toList();
                return new SelectionEventListener.SelectionResponse(eb, row);
            }
        } else {
            // One of the others selectors. Means we are still in the same category
            Optional<MyCommand<?>> command = categoryMap.values().stream().flatMap(Collection::stream).filter(z -> z.getAliases().get(0).equals(selected)).findFirst();
            assert command.isPresent();
            MyCommand<?> c = command.get();
            SpecificCommandHelp help = new SpecificCommandHelp(c, e.getPrefix());

            List<CommandCategory> categories = categoryMap.keySet().stream().sorted(Comparator.comparingInt(CommandCategory::getOrder)).toList();
            CommandCategory category = c.getCategory();
            ActionRow categoriesRow = buildMenuCategory(categories, category.name());
            List<ActionRow> commands = buildMenuCommand(eb, e.getPrefix(), category, c);
            eb.addBlankField(false);
            List<ActionRow> row = Stream.concat(Stream.of(categoriesRow), commands.stream()).toList();


            String usage = help.usage;
            String[] split = usage.split("\n");
            String header = "**Usage:**";
            String field = "";
            assert split.length > 0;
            header += " " + split[0];
            if (split.length != 1) {
                String[] lines = Arrays.copyOfRange(split, 1, split.length);
                field = String.join("\n", lines);
            }
            eb.addField("**Name:**", help.name, false)
                    .addField("**Description:**", help.description, false)
                    .addField("**Aliases:**", help.aliases, false);
            List<String> pages = TextSplitter.split(field, 1024);
            int count = 0;
            for (String page : pages) {
                if (count++ == 0) {
                    eb.addField(header, page, false);
                } else {
                    eb.addField(EmbedBuilder.ZERO_WIDTH_SPACE, page, false);
                }
            }

            return new SelectionEventListener.SelectionResponse(eb, row);
        }
    }


    @NotNull
    private ActionRow buildMenuCategory(List<CommandCategory> categories, String value) {
        SelectMenu.Builder category = SelectMenu.create("category_id")
                .setPlaceholder("Select a command of the %s category to see the details of that command".formatted(value))
                .setMinValues(1)
                .addOption("All commands", CUSTOM_ALL, "A list of all the commands available in the bot")
                .addOptions(categories.stream().map(z -> SelectOption.of(WordUtils.capitalizeFully(z.name().replaceAll("_", " ")), z.name()).withDescription(StringUtils.abbreviate(z.getDescription(), 100))).toList())
                .setPlaceholder("Select a category to its commands");

        category.setDefaultOptions(List.of(SelectOption.of("test", value)));
        return ActionRow.of(category.build());
    }

    private List<ActionRow> buildMenuCommand(EmbedBuilder eb, char prefix, CommandCategory commandCategory, @Nullable MyCommand<?> active) {
        SortedSet<MyCommand<?>> commandList = categoryMap.get(commandCategory);
        buildCategory(eb, prefix, commandCategory, commandList);
        List<List<MyCommand<?>>> commandsPartitions = Lists.partition(new ArrayList<>(commandList), 25);

        AtomicInteger ranker = new AtomicInteger(0);

        return (commandsPartitions.stream().map(z -> ActionRow.of(SelectMenu.create(String.valueOf(ranker.incrementAndGet()))
                .addOptions(z.stream().map(w -> SelectOption.of(w.getName(), w.getAliases().get(0))
                        .withDefault(active == w)
                        .withDescription(StringUtils.abbreviate(w.getDescription(), 100))).toList())
                .setPlaceholder(commandsPartitions.size() > 1 ? "Commands %d-%d".formatted(((ranker.get() - 1) * 25) + 1, Math.min(ranker.get() * 25, commandList.size())) : "Commands")
                .setMinValues(1).build())).toList());

    }

    public void sendPrivate(MessageChannel channel, Context e) {
        Character prefix = e.getPrefix();
        StringBuilder s = new StringBuilder();
        List<RestAction<Message>> messageActions = new ArrayList<>();
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

        for (Map.Entry<CommandCategory, SortedSet<MyCommand<?>>> a : categoryMap.entrySet()) {
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

    public void sendEmbed(Context e) {
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);
        Character correspondingPrefix = e.getPrefix();
        for (Map.Entry<CommandCategory, SortedSet<MyCommand<?>>> a : categoryMap.entrySet()) {
            buildCategory(embedBuilder, correspondingPrefix, a.getKey(), a.getValue());
        }
        embedBuilder.setFooter(correspondingPrefix + "help \"command\" for the explanation of one command.\n" + correspondingPrefix + "help --all for the whole help message")
                .setTitle("Commands");
        e.sendMessage(embedBuilder.build()).queue();
    }

    private void buildCategory(EmbedBuilder embedBuilder, Character correspondingPrefix, CommandCategory category, SortedSet<MyCommand<?>> commands) {
        String line = commands.stream().map(x -> "*" + correspondingPrefix + x.getAliases().get(0) + "*").collect(Collectors.joining(", "));
        embedBuilder.addField(new MessageEmbed.Field("\n__**" + category.toString().replaceAll("_", " ") + ":**__ _" + category.getDescription() + "_\n", line, false));
    }

    private void doSend(String command, Context e, Character prefix) {
        List<MyCommand<?>> values = categoryMap.values().stream().flatMap(Collection::stream).toList();
        for (MyCommand<?> c : values) {
            if (c.getAliases().contains(command.toLowerCase())) {
                SpecificCommandHelp help = new SpecificCommandHelp(c, prefix);

                String realUsageInstructions = help.usage;
                List<String> pagees = TextSplitter.split(realUsageInstructions, 1500);
                e.sendMessage("**Name:** " + help.name + "\n" +
                        "**Description:** " + help.description + "\n" +
                        "**Aliases:** " + help.aliases + "\n" +
                        "**Usage:** " +
                        prefix + pagees.get(0)).queue(x -> {
                    for (int i = 1; i < pagees.size(); i++) {
                        e.sendMessage(EmbedBuilder.ZERO_WIDTH_SPACE + pagees.get(i)).queue();
                    }
                });
                return;
            }
        }
        e.sendMessage("The provided command '**" + command +
                "**' does not exist. Use " + prefix + "help to list all commands.").queue();
    }

    private record SpecificCommandHelp(String name, String description, String aliases, String usage) {
        public SpecificCommandHelp(MyCommand<?> command, char prefix) {
            this((command.getName() == null || command.getName().isEmpty()) ? NO_NAME : command.getName(),
                    (command.getDescription() == null || command.getDescription().isEmpty()) ? NO_DESCRIPTION : command.getDescription(),
                    prefix + String.join(", " + prefix, command.getAliases()),
                    command.getUsageInstructions());
        }

        @Override
        public String usage() {
            return (usage == null || usage
                    .isEmpty()) ? NO_USAGE : usage;
        }
    }


}
