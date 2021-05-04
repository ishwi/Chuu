package core.commands.moderation;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.interactions.InteractionBuilder;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class RefreshSlashCommand extends ConcurrentCommand<CommandParameters> {

    public RefreshSlashCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_INFO;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
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
            if (t.getOwner().getIdLong() != e.getAuthor().getIdLong()) {
                InteractionBuilder.setGlobalCommands(e.getJDA()).queue(z -> sendMessageQueue(e, "Finished the refresh!"));
            }
        });
    }
}
