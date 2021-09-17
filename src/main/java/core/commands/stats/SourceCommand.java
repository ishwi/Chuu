package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;

import javax.annotation.Nonnull;
import java.util.List;

public class SourceCommand extends ConcurrentCommand<CommandParameters> {
    public static final String REPO_URL = "https://github.com/ishwi/chuu";


    public SourceCommand(ServiceView dao) {
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
        return "Source code of the bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("github", "source", "repo");
    }

    @Override
    public String getName() {
        return "GitHub";
    }

    @Override
    protected void onCommand(Context e, @Nonnull CommandParameters params) {
        sendMessageQueue(e, String.format("This is the GitHub link of the bot:%n%s", REPO_URL));
    }
}
