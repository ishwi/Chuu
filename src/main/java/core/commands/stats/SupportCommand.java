package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.MyCommand;
import core.commands.utils.CommandCategory;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SupportCommand extends MyCommand<CommandParameters> {
    public SupportCommand(ServiceView dao) {
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
        return "Link to the discord server where you can contact the bot developers";
    }

    @Override
    public List<String> getAliases() {
        return List.of("support");
    }

    @Override
    public String getName() {
        return "Support";
    }

    @Override
    public void onCommand(Context e, @NotNull CommandParameters params) {
        sendMessageQueue(e, "If you found bugs, have issues with the bots, want to request features or simply want to talk you can find us here:\nhttps://discord.gg/3tYsPMWvQG");
    }
}
