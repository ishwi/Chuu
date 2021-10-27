package core.commands.config;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.abstracts.MyCommand;
import core.commands.stats.SourceCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.Parser;
import core.parsers.QueryParser;
import core.parsers.params.WordParameter;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SearchCommand extends ConcurrentCommand<WordParameter> {
    public SearchCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_INFO;
    }

    @Override
    public Parser<WordParameter> initParser() {
        return new QueryParser(true);
    }

    @Override
    public String getDescription() {
        return "Search commands by name";
    }

    @Override
    public List<String> getAliases() {
        return List.of("search");
    }

    @Override
    public String getName() {
        return "Command search";
    }

    @Override
    public void onCommand(Context e, @Nonnull WordParameter params) throws LastFmException, InstanceNotFoundException {
        String word = params.getWord().toLowerCase(Locale.ROOT);
        List<? extends MyCommand<?>> strings;
        String title;
        String footer;
        if (word.isBlank()) {
            title = "All commands";
            strings = e.getJDA().getRegisteredListeners().stream().filter(z -> z instanceof MyCommand<?>).map(z -> (MyCommand<?>) z).toList();
            footer = "Use a search term to refine your search!";
        } else {
            List<MyCommand<?>> found = new ArrayList<>();
            List<? extends MyCommand<?>> myCommands = e.getJDA().getRegisteredListeners().stream().filter(z -> z instanceof MyCommand).map(z -> (MyCommand<?>) z).toList();

            myCommands.stream().filter(z -> z.getName().toLowerCase(Locale.ROOT).contains(word)).forEach(found::add);
            myCommands = myCommands.stream().filter(Predicate.not(found::contains)).toList();
            myCommands.stream().filter(z -> z.getAliases().stream().anyMatch(l -> l.toLowerCase(Locale.ROOT).contains(word))).forEach(found::add);
            myCommands = myCommands.stream().filter(Predicate.not(found::contains)).toList();
            myCommands.stream().filter(z -> z.getDescription().toLowerCase(Locale.ROOT).contains(word)).forEach(found::add);

            strings = found;
            if (found.size() > 10) {
                footer = "Found %s commands!".formatted(found.size());
            } else {
                footer = "";
            }
            title = "Commands found by: " + word;

        }

        if (strings.isEmpty()) {
            sendMessageQueue(e, "Didn't find any command searching by: **" + word + "**");
            return;
        }


        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, e.getAuthor().getIdLong());
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(title, SourceCommand.REPO_URL, uInfo.urlImage())
                .setFooter(footer);
        new PaginatorBuilder<>(e, embedBuilder, strings)
                .mapper(z -> "**%s**: %s(%s)%n".formatted(z.getName(), z.getDescription(), z.getAliases().stream().limit(3).collect(Collectors.joining(";"))))
                .unnumered().build().queue();


    }
}
