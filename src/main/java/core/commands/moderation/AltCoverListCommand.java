package core.commands.moderation;

import core.Chuu;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import core.services.CoverService;
import dao.ChuuService;
import dao.entities.CoverItem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AltCoverListCommand extends ConcurrentCommand<CommandParameters> {
    public AltCoverListCommand(ChuuService dao) {
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
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) {
        CoverService coverService = Chuu.getCoverService();
        Map<CoverItem, Integer> counts = coverService.getCounts();
        List<String> str = counts.entrySet().stream().map((t -> "**%s - %s**: %d %s%n".formatted(t.getKey().artist(), t.getKey().album(), t.getValue(), CommandUtil.singlePlural(t.getValue(), "cover", "covers"))))
                .sorted(String.CASE_INSENSITIVE_ORDER).toList();
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < str.size(); i++) {
            a.append("# ").append(str.get(i));
        }


        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setDescription(a)
                .setColor(ColorService.computeColor(e))
                .setTitle("Alt covers");

        e.getChannel().sendMessage(new MessageBuilder()
                .setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(str, message1, embedBuilder));


    }
}
