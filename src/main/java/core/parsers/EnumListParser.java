package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.commands.abstracts.MyCommand;
import core.commands.utils.CommandUtil;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.EnumListParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.text.WordUtils;

import java.util.*;
import java.util.function.Function;

public class EnumListParser<T extends Enum<T>> extends DaoParser<EnumListParameters<T>> implements Generable<EnumListParameters<T>> {
    protected final Class<T> clazz;
    private final String name;
    private final EnumSet<T> excluded;
    private final Function<String, EnumSet<T>> mapper;


    public EnumListParser(ChuuService db, String name, Class<T> tClass, EnumSet<T> excluded, Function<String, EnumSet<T>> mapper) {
        super(db);
        this.name = name;
        this.clazz = tClass;
        this.excluded = excluded;
        this.mapper = mapper;
    }

    @Override
    protected void setUpErrorMessages() {

    }

    public SlashCommandData generateCommandData(MyCommand<?> myCommand) {
        SlashCommandData commandData = Commands.slash(myCommand.slashName(), myCommand.getDescription());

        SubcommandData set = new SubcommandData("set", "Replaces all your " + name + " with the ones provided");
        set.addOption(OptionType.STRING, name, "List of all " + name + " to set", true);

        SubcommandData list = new SubcommandData("list", "List the setted " + name + " for an user");
        list.addOptions(new StrictUserExplanation().explanation().options());


        SubcommandData add = new SubcommandData("add", "Adds only one " + name);
        SubcommandData remove = new SubcommandData("remove", "Removes only one " + name);
        SubcommandData help = new SubcommandData("help", "Removes only one " + name);
        help.addOption(OptionType.BOOLEAN, "all", "whether should include the help for one command or for all");


        EnumSet<T> ts = EnumSet.complementOf(excluded);
        List<List<T>> partition = ListUtils.partition(new ArrayList<>(ts), 25);
        int size = partition.size();
        List<OptionData> options = new ArrayList<>();
        for (int i = 0, partitionSize = partition.size(); i < partitionSize; i++) {
            String mode;
            String nameOpt;
            if (size == 1) {
                mode = "All modes to select to";
                nameOpt = name;

            } else {
                mode = (i + 1) + CommandUtil.getRank(i + 1) + " batch of " + name;
                nameOpt = name + (i + 1);
            }
            OptionData optionData = new OptionData(OptionType.STRING, nameOpt, mode);
            if (size == 1) {
                optionData.setRequired(true);
            }
            optionData.addChoices(partition.get(i).stream().map(z -> new Command.Choice(z.toString(), z.name())).toList());
            options.add(optionData);
        }
        options.stream().sorted(Comparator.comparing(OptionData::isRequired)).toList().forEach(w -> {
            OptionData unrequired = new OptionData(w.getType(), w.getName(), w.getDescription())
                    .addChoices(w.getChoices()).setRequired(false);
            help.addOptions(unrequired);
            add.addOptions(w);
            remove.addOptions(w);
        });
        commandData.addSubcommands(add);
        commandData.addSubcommands(remove);
        commandData.addSubcommands(set);
        commandData.addSubcommands(help);
        commandData.addSubcommands(list);
        return commandData;
    }

    @Override
    public EnumListParameters<T> parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) {
        CommandInteraction e = ctx.e();
        String subcommandName = e.getSubcommandName();
        User user = InteractionAux.parseUser(e);
        assert subcommandName != null;
        return switch (subcommandName) {
            case "add" -> parseSlash(ctx, e, user, true, false, false);
            case "remove" -> parseSlash(ctx, e, user, false, true, false);
            case "set" -> parseSlash(ctx, e, user, false, false, false);
            case "list" -> new EnumListParameters<>(ctx, EnumSet.noneOf(clazz), false, true, false, false, user);
            case "help" -> {
                boolean isAll = Optional.ofNullable(e.getOption("all")).map(OptionMapping::getAsBoolean).orElse(false);
                if (isAll) {
                    EnumSet<T> building = EnumSet.complementOf(excluded);
                    yield new EnumListParameters<>(ctx, building, true, false, false, false, user);
                } else {
                    yield parseSlash(ctx, e, user, false, false, true);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + subcommandName);
        };
    }

    private EnumListParameters<T> parseSlash(InteracionReceived<? extends CommandInteraction> ctx, CommandInteraction e, User user, boolean isAdding, boolean isRemoving, boolean isHelp) {

        EnumSet<T> ts = EnumSet.complementOf(excluded);
        List<List<T>> partition = CommandUtil.partition(new ArrayList<>(ts), 25);
        int size = partition.size();

        EnumSet<T> building;
        if (size == 1) {
            building = Optional.ofNullable(e.getOption(name)).map(OptionMapping::getAsString).map(mapper).orElse(EnumSet.noneOf(clazz));
        } else {
            Optional<String> opt = Optional.empty();
            for (int i = 0, partitionSize = partition.size(); i < partitionSize; i++) {
                List<T> tList = partition.get(i);
                final int j = i + 1;
                opt = opt.or(() -> Optional.ofNullable(e.getOption(name + (j))).map(OptionMapping::getAsString));
                if (opt.isEmpty()) {
                    continue;
                }
                break;
            }
            if (opt.isEmpty()) {
                sendError("You need to select one " + name, ctx);
                return null;
            }
            building = mapper.apply(opt.get());
        }
        if (building.isEmpty()) {
            return new EnumListParameters<>(ctx, building, true, true, isAdding, isRemoving, e.getUser());
        }
        return new EnumListParameters<>(ctx, building, false, false, isAdding, isRemoving, e.getUser());
    }

    @Override
    protected EnumListParameters<T> parseLogic(Context e, String[] words) throws InstanceNotFoundException {
        EnumSet<T> building = EnumSet.noneOf(clazz);
        ParserAux parserAux = new ParserAux(words);
        User oneUser = parserAux.getOneUserPermissive(e, dao);
        words = parserAux.getMessage();
        if (words.length == 0) {
            return new EnumListParameters<>(e, building, false, true, false, false, oneUser);
        }

        String command = words[0];
        if (command.equalsIgnoreCase("help")) {

            if (words.length > 1) {
                if (words[1].equals("all")) {
                    building = EnumSet.complementOf(excluded);
                } else {
                    String remaining = String.join(" ", Arrays.copyOfRange(words, 1, words.length));
                    building = mapper.apply(remaining);
                }
            }
            return new EnumListParameters<>(e, building, true, false, false, false, oneUser);
        } else if (command.equalsIgnoreCase("add")) {
            return parse(1, words, e, true, false);
        } else if (command.equalsIgnoreCase("remove")) {
            return parse(1, words, e, false, true);
        } else if (command.equalsIgnoreCase("list")) {
            return new EnumListParameters<>(e, building, false, true, false, false, oneUser);
        } else {
            return parse(0, words, e, false, false);
        }
    }

    private EnumListParameters<T> parse(int index, String[] words, Context e, boolean isAdding, boolean isRemoving) {
        String remaining = String.join(" ", Arrays.copyOfRange(words, index, words.length));
        EnumSet<T> building = mapper.apply(remaining);
        if (building.isEmpty()) {
            return new EnumListParameters<>(e, building, true, true, isAdding, isRemoving, e.getAuthor());
        }
        return new EnumListParameters<>(e, building, false, false, isAdding, isRemoving, e.getAuthor());
    }


    @Override
    public List<Explanation> getUsages() {
        EnumSet<T> set = EnumSet.complementOf(excluded);
        List<String> lines = set.stream().map(x -> WordUtils.capitalizeFully(x.name().replaceAll("_", "-"), '-')).toList();
        String join = String.join("** | **", lines);
        String usage = "\t Writing **__help__** will give you a brief description of all the " + name + " that you include in the command or alternatively all the options with **__help__**\n" +
                       "Writing **__list__** will give you all your current set " + name + "\n" +
                       "Writing **__add__** will add the inputted " + name + " instead of replacing\n" +
                       "Writing **__remove__** will remove the inputted " + name + " instead of replacing\n";
        OptionData optionData = new OptionData(OptionType.STRING, "auxiliar", "auxiliar options");
        optionData.addChoice("help", "help");
        optionData.addChoice("help-all", "help-all");
        optionData.addChoice("add", "add");
        optionData.addChoice("remove", "remove");
        optionData.addChoice("list", "list");
        OptionData optionData2 = new OptionData(OptionType.STRING, "values", "the values to select");
        lines.stream().limit(25).forEach(t -> optionData2.addChoice(t, t));
        Explanation explanation = () -> new ExplanationLine("[help|help all|list|add|remove] " + name, usage, optionData);
        Explanation explanation2 = () -> new ExplanationLine("config value ", join, optionData2);
        return List.of(explanation, explanation2);


    }

}
