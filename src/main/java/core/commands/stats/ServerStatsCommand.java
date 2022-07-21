package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;
import dao.ServerStats;
import dao.entities.ObscurityEntry;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.List;

public class ServerStatsCommand extends ConcurrentCommand<CommandParameters> {
    public ServerStatsCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Stats about this server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("serverstats", "sstats");
    }

    @Override
    public String slashName() {
        return "stats";
    }

    @Override
    public String getName() {
        return "Server Stats";
    }

    @Override
    public void onCommand(Context e, @Nonnull CommandParameters params) {
        ServerStats serverStats = db.getServerStats(e.getGuild().getIdLong());

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(e.getGuild().getName() + "'s stats", null, e.getGuild().getIconUrl())
                .addField("**Users on server:**", "**" + serverStats.memberCount() + "**", true)
                .addField("**Commands used:**", "**" + serverStats.commmandCount() + "**", true)
                .addField("**Top command:**", "" + serverStats.topCommand() + ": " + serverStats.countOnTopCommand() + " uses", true)
                .addField("**Random urls submitted:**", "**" + serverStats.randomCount() + "**", true)
                .addField("**Number of Votes casted:**", "**" + serverStats.voteCount() + "**", true)
                .addField("**Recommendations Given:**", "**" + serverStats.recCount() + "**", true)
                .addField("**Total Artist Images:**", "**" + serverStats.imageCount() + "**", true)
                .addField("**Obscurity Rank:**", "**%d%s/%d**".formatted(serverStats.stats().rank(), CommandUtil.getRank(serverStats.stats().rank()), serverStats.stats().total()), true)
                .addField("**Average obscurity points:**", "**" + ObscurityEntry.average.format(100 - serverStats.stats().averageScore()) + "**", true);

        e.sendMessage(embedBuilder.build()).queue();


    }

}
