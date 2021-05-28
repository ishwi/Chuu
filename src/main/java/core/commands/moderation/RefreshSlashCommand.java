package core.commands.moderation;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.abstracts.MyCommand;
import core.commands.utils.CommandCategory;
import core.interactions.InteractionBuilder;
import core.parsers.NoOpParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import net.dv8tion.jda.api.requests.RestAction;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class RefreshSlashCommand extends ConcurrentCommand<CommandParameters> {

    public RefreshSlashCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        NoOpParser noOpParser = new NoOpParser();
        noOpParser.addOptional(new OptionalEntity("server", "refresh only this server"));
        noOpParser.addOptional(new OptionalEntity("global", "global refresh"));
        noOpParser.addOptional(new OptionalEntity("delete", "clean this server"));
        noOpParser.addOptional(new OptionalEntity("globaldelete", "clean the bot"));
        noOpParser.addOptional(new OptionalEntity("missing", "missing"));
        return noOpParser;
    }

    @Override
    public String getDescription() {
        return "Refresh slash commands";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("slashrefresh");
    }

    @Override
    public String getName() {
        return "Refresh Slash commmands";
    }

    @Override
    protected void onCommand(Context e, @NotNull CommandParameters params) {
        e.getJDA().retrieveApplicationInfo().queue(t -> {
            if (t.getOwner().getIdLong() == e.getAuthor().getIdLong()) {
                if (params.hasOptional("delete")) {
                    e.getGuild().retrieveCommands().flatMap(z -> RestAction.allOf(z.stream().map(l -> e.getGuild().deleteCommandById(l.getId())).toList())).queue(z -> sendMessageQueue(e, "Finished the deletion!"));
                } else if (params.hasOptional("server")) {
                    InteractionBuilder.setServerCommand(e.getGuild()).queue(z -> sendMessageQueue(e, "Finished the refresh!"));
                } else if (params.hasOptional("globaldelete")) {
                    e.getJDA().retrieveCommands().flatMap(z -> RestAction.allOf(z.stream().map(l -> e.getJDA().deleteCommandById(l.getId())).toList())).queue(z -> sendMessageQueue(e, "Finished the global deletion!"));
                } else if (params.hasOptional("global")) {
                    InteractionBuilder.setGlobalCommands(e.getJDA()).queue(z -> sendMessageQueue(e, "Finished the global refresh!"));
                } else if (params.hasOptional("missing")) {
                    e.sendMessage(e.getJDA().getRegisteredListeners().stream()
                            .filter(w -> w instanceof MyCommand<?>)
                            .map(w -> (MyCommand<?>) w)
                            .sorted(Comparator.comparingInt(w -> w.order))
                            .filter(not(InteractionBuilder.test))
                            .map(MyCommand::getName).collect(Collectors.joining(", "))).queue();
                } else {
                    sendMessageQueue(e, "Nothing done :)");
                }
            }
        });
    }
}
