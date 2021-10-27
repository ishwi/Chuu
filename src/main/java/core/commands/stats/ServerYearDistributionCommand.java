package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ServerYearDistributionCommand extends ConcurrentCommand<CommandParameters> {


    public ServerYearDistributionCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = true;

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
        return "Which year are your albums from?";
    }

    @Override
    public List<String> getAliases() {
        return List.of("serveryears", "serveryear", "syear", "syears");
    }

    @Override
    public String slashName() {
        return "years";
    }

    @Override
    public String getName() {
        return "Server album years";
    }


    @Override
    public void onCommand(Context e, @Nonnull CommandParameters params) {
        var yearCounts = db.getGuildYears(e.getGuild().getIdLong());
        List<String> lines = yearCounts.entrySet().stream().sorted(Map.Entry.comparingByValue((Comparator.reverseOrder()))).map(t ->
                ". **%d**: %d %s%n".formatted(t.getKey().getValue(), t.getValue(), CommandUtil.singlePlural(t.getValue(), "album", "albums"))
        ).toList();

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s's years", e.getGuild().getName()), null, e.getGuild().getIconUrl())
                .setFooter("%s has albums from %d different %s".formatted(CommandUtil.stripEscapedMarkdown(e.getGuild().getName()), yearCounts.size(), CommandUtil.singlePlural(yearCounts.size(), "year", "years")), null);

        new PaginatorBuilder<>(e, embedBuilder, lines).build().queue();
    }


}
