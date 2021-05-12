package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import dao.ChuuService;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GlobalDecadeDistributionCommand extends ConcurrentCommand<CommandParameters> {


    public GlobalDecadeDistributionCommand(ChuuService dao) {
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
    public String getName() {
        return "Global album decades";
    }


    @Override
    protected void onCommand(Context e, @NotNull CommandParameters params) {
        var counts = db.getGlobalDecades();
        List<String> lines = counts.entrySet().stream().sorted(Map.Entry.comparingByValue((Comparator.reverseOrder()))).map(t ->
                ". **%ds**: %d %s%n".formatted(CommandUtil.getDecade(t.getKey().getValue()), t.getValue(), CommandUtil.singlePlural(t.getValue(), "album", "albums"))
        ).toList();

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < lines.size() && i < 10; i++) {
            String s = lines.get(i);
            a.append(i + 1).append(s);
        }

        String name = e.getJDA().getSelfUser().getName();
        var embedBuilder = new ChuuEmbedBuilder()
                .setDescription(a)
                .setAuthor(String.format("%s's years", name), null, e.getJDA().getSelfUser().getAvatarUrl())
                .setColor(ColorService.computeColor(e))
                .setFooter("%s has albums from %d different %s".formatted(CommandUtil.markdownLessString(name), counts.size(), CommandUtil.singlePlural(counts.size(), "decade", "decades")), null);

        e.sendMessage(embedBuilder.build()).queue(m ->
                new Reactionary<>(lines, m, 10, embedBuilder));
    }


}
