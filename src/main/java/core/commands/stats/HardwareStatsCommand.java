package core.commands.stats;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.BotStats;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.util.List;

public class HardwareStatsCommand extends ConcurrentCommand<CommandParameters> {
    public HardwareStatsCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Stats about the bot internals";
    }

    @Override
    public List<String> getAliases() {
        return List.of("botstats");
    }

    @Override
    public String getName() {
        return "Bot Stats";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        BotStats botStats = getService().getBotStats();
        int shardTotal = e.getJDA().getShardInfo().getShardTotal();
        int mb = 1024 * 1024;
        Runtime instance = Runtime.getRuntime();
        long l = (instance.totalMemory() - instance.freeMemory()) / mb;
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(e.getJDA().getSelfUser().getName() + "'s stats")
                .setColor(CommandUtil.randomColor())
                .addField("**Registered users:**", "**" + botStats.getUser_count() + "**", true)
                .addField("**Number of Servers:**", "**" + botStats.getGuild_count() + "**", true)
                .addField("**Total setted users:**", "**" + botStats.getSet_count() + "**", true)
                .addField("**Artist Count:**", "**" + botStats.getArtistCount() + "**", true)
                .addField("**Album Count:**", "**" + botStats.getAlbum_count1() + "**", true)
                .addField("**Scrobble Count:**", "**" + botStats.getScrobbled_count() + "**", true)
                .addField("**RYM Rating Count:**", "**" + botStats.getRym_count() + "**", true)
                .addField("**Average RYM Rating:**", "**" + new DecimalFormat("#0.##").format(botStats.getRym_avg() / 2f) + "**", true)
                .addField("**Recommendations Given:**", "**" + botStats.getRecommendation_count() + "**", true)
                .addField("**Count of Random Urls:**", "**" + botStats.getRandom_count() + "**", true)
                .addField("**Total Artist Images:**", "**" + botStats.getImage_count() + "**", true)
                .addField("**Number of Votes casted:**", "**" + botStats.getVote_count() + "**", true)
                .addField("**Memory usage:**", "**" + l + " MB**", true)
                .addField("**Total Number of api requests:**", "**" + botStats.getApi_count() + "**", true)
                .addField("**Shards Count:**", "**" + (shardTotal) + "**", true)
                .setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
        e.getChannel().sendMessage(embedBuilder.build()).queue();


    }

}
