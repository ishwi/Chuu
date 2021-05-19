package core.commands.leaderboards;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.LeaderboardCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ObscurityLeaderboardCommand extends LeaderboardCommand<CommandParameters> {
    public static final boolean disabled = true;
    private final AtomicInteger maxConcurrency = new AtomicInteger(4);


    public ObscurityLeaderboardCommand(ServiceView dao) {
        super(dao, true);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_LEADERBOARDS;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getEntryName(CommandParameters params) {
        return "Obscurity points";
    }

    @Override
    public String getDescription() {
        return "Gets how \\*obscure\\* your scrobbled artist are in relation with all the rest of the users of the server";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("obscuritylb", "ob", "obs");
    }

    @Override
    public List<LbEntry> getList(CommandParameters params) {
        return db.getObscurityRankings(params.getE().getGuild().getIdLong());

    }

    @Override
    protected boolean handleCommand(Context e) {
        if (disabled) {
            sendMessageQueue(e, "This command has been temporarily disabled :(");
            return false;
        }
        if (maxConcurrency.decrementAndGet() == 0) {
            sendMessageQueue(e, "There are a lot of people executing this command right now, try again later :(");
            maxConcurrency.incrementAndGet();
            return true;
        } else {
            CompletableFuture<Message> future = null;
            try {
                future = sendMessage(e, "Obscurity command can take a really long time :(").submit();
                super.handleCommand(e);
                return true;
            } catch (Throwable ex) {
                Chuu.getLogger().warn(ex.getMessage(), ex);
                return false;
            } finally {
                maxConcurrency.incrementAndGet();
                CommandUtil.handleConditionalMessage(future);
            }
        }

    }


    @Override
    public String getName() {
        return "Obscurity";
    }
}
