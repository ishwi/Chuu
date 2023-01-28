package core.commands.moderation;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.CoverService;
import core.util.ServiceView;
import dao.entities.CoverItem;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AltCoverListCommand extends ConcurrentCommand<CommandParameters> {
    public AltCoverListCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "All albums that have covers";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("coverlists", "altlist");
    }

    @Override
    public String getName() {
        return "Alt cover list";
    }

    @Override
    public void onCommand(Context e, @NotNull CommandParameters params) {
        CoverService coverService = Chuu.getCoverService();
        Map<CoverItem, Integer> counts = coverService.getCounts();

        List<String> str = counts.entrySet().stream().map((t -> "# **%s - %s**: %d %s%n".formatted(t.getKey().artist(), t.getKey().album(), t.getValue(), CommandUtil.singlePlural(t.getValue(), "cover", "covers"))))
                .sorted(String.CASE_INSENSITIVE_ORDER).toList();

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setTitle("Alt covers");

        new PaginatorBuilder<>(e, embedBuilder, str).unnumered().build().queue();


    }
}
