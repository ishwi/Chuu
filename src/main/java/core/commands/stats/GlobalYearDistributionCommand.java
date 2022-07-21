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
import core.util.ServiceView;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GlobalYearDistributionCommand extends ConcurrentCommand<CommandParameters> {


    public GlobalYearDistributionCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = true;

    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;

    }

    @Override
    public String getDescription() {
        return "Which year are the bot albums from?";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalyears", "globalyear", "gyear", "gyears");
    }

    @Override
    public String slashName() {
        return "years";
    }

    @Override
    public String getName() {
        return "Global album years";
    }


    @Override
    public void onCommand(Context e, @Nonnull CommandParameters params) {
        var yearCount = db.getGlobalYears();
        List<String> lines = yearCount.entrySet().stream().sorted(Map.Entry.comparingByValue((Comparator.reverseOrder()))).map(t ->
                ". **%d**: %d %s%n".formatted(t.getKey().getValue(), t.getValue(), CommandUtil.singlePlural(t.getValue(), "album", "albums"))
        ).toList();
        String name = e.getJDA().getSelfUser().getName();

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s's years", name), null, e.getJDA().getSelfUser().getAvatarUrl())
                .setFooter("%s has albums from %d different %s".formatted(CommandUtil.stripEscapedMarkdown(name), yearCount.size(), CommandUtil.singlePlural(yearCount.size(), "year", "years")), null);

        new PaginatorBuilder<>(e, embedBuilder, lines).build().queue();
    }


}
