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

public class GlobalDecadeDistributionCommand extends ConcurrentCommand<CommandParameters> {


    public GlobalDecadeDistributionCommand(ServiceView dao) {
        super(dao);

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
        return "Which decade are the bot albums from?";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globaldecades", "globaldecade", "gdecade", "gdecades");
    }

    @Override
    public String slashName() {
        return "decades";
    }

    @Override
    public String getName() {
        return "Global album decades";
    }


    @Override
    public void onCommand(Context e, @Nonnull CommandParameters params) {
        var counts = db.getGlobalDecades();
        List<String> lines = counts.entrySet().stream().sorted(Map.Entry.comparingByValue((Comparator.reverseOrder()))).map(t ->
                ". **%ds**: %d %s%n".formatted(CommandUtil.getDecade(t.getKey().getValue()), t.getValue(), CommandUtil.singlePlural(t.getValue(), "album", "albums"))
        ).toList();

        String name = e.getJDA().getSelfUser().getName();
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s's years", name), null, e.getJDA().getSelfUser().getAvatarUrl())
                .setFooter("%s has albums from %d different %s".formatted(CommandUtil.stripEscapedMarkdown(name), counts.size(), CommandUtil.singlePlural(counts.size(), "decade", "decades")), null);

        new PaginatorBuilder<>(e, embedBuilder, lines).build().queue();
    }


}
