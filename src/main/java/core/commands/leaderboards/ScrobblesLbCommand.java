package core.commands.leaderboards;

import core.commands.Context;
import core.commands.abstracts.LeaderboardCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

public class ScrobblesLbCommand extends LeaderboardCommand<CommandParameters, Integer> {
    public ScrobblesLbCommand(ServiceView dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_LEADERBOARDS;
    }

    @Override
    public String getEntryName(CommandParameters params) {
        return "scrobbles";
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Users ordered by scrobbles";
    }

    @Override
    public List<String> getAliases() {
        return List.of("scrobbleslb", "slb", "scrobbledlb");
    }

    @Override
    public String getName() {
        return "Scrobble leaderboard";
    }

    @Override
    public List<LbEntry<Integer>> getList(CommandParameters params) {
        return db.getScrobblesLeaderboard(params.getE().getGuild().getIdLong());
    }

    @Override
    protected void setFooter(EmbedBuilder embedBuilder, List<LbEntry<Integer>> list, CommandParameters params) {
        Context e = params.getE();
        embedBuilder.setFooter(e.getGuild().getName() + " has " + list.size() + CommandUtil.singlePlural(list.size(), "user", "users") + " with scrobbles!\n", null);
    }
}
