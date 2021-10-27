package core.commands.moderation;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.abstracts.MyCommand;
import core.commands.utils.CommandCategory;
import core.interactions.InteractionBuilder;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.utils.OptionalEntity;
import core.parsers.utils.Optionals;
import dao.ServiceView;

import javax.annotation.Nonnull;
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
        return new NoOpParser(
                Optionals.SERVER.opt.withDescription("refresh only this server"),
                Optionals.GLOBAL.opt.withDescription("global refresh"),
                new OptionalEntity("delete", "clean this server"),
                new OptionalEntity("globaldelete", "clean the bot"),
                new OptionalEntity("missing", "missing")
        );
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
    public void onCommand(Context e, @Nonnull CommandParameters params) {
        e.getJDA().retrieveApplicationInfo().queue(t -> {
            if (t.getOwner().getIdLong() == e.getAuthor().getIdLong()) {
                if (params.hasOptional("delete")) {
                    e.getGuild().updateCommands().queue(z -> sendMessageQueue(e, "Finished the server deletion!"), throwable -> sendMessageQueue(e, throwable.getMessage()));
                } else if (params.hasOptional("server")) {
                    InteractionBuilder.setServerCommand(e.getGuild()).queue(z -> sendMessageQueue(e, "Finished the refresh!"), throwable -> sendMessageQueue(e, throwable.getMessage()));
                } else if (params.hasOptional("globaldelete")) {
                    e.getJDA().updateCommands().queue(z -> sendMessageQueue(e, "Finished the global deletion!"), throwable -> sendMessageQueue(e, throwable.getMessage()));
                } else if (params.hasOptional("global")) {
                    InteractionBuilder.setGlobalCommands(e.getJDA()).queue(z -> sendMessageQueue(e, "Finished the refresh!"), throwable -> sendMessageQueue(e,
                            throwable.getMessage()));
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
