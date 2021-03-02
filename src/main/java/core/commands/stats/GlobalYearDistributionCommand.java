package core.commands.stats;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GlobalYearDistributionCommand extends ConcurrentCommand<CommandParameters> {


    public GlobalYearDistributionCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = true;

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
        return "Which year are the bot albums from?";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalyears", "globalyear", "gyear", "gyears");
    }

    @Override
    public String getName() {
        return "Global album years";
    }


    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        var counts = db.getGlobalYears();
        List<String> collect = counts.entrySet().stream().sorted(Map.Entry.comparingByValue((Comparator.reverseOrder()))).map(t ->
                ". **%d**: %d %s%n".formatted(t.getKey().getValue(), t.getValue(), CommandUtil.singlePlural(t.getValue(), "album", "albums"))
        ).collect(Collectors.toList());

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < collect.size() && i < 10; i++) {
            String s = collect.get(i);
            a.append(i + 1).append(s);
        }
        String name = e.getJDA().getSelfUser().getName();

        var embedBuilder = new EmbedBuilder()
                .setDescription(a)
                .setAuthor(String.format("%s's years", name), null, e.getJDA().getSelfUser().getAvatarUrl())
                .setColor(ColorService.computeColor(e))
                .setFooter("%s has albums from %d different %s".formatted(CommandUtil.markdownLessString(name), counts.size(), CommandUtil.singlePlural(counts.size(), "year", "years")), null);

        e.getChannel().sendMessage(embedBuilder.build()).queue(m ->
                new Reactionary<>(collect, m, 10, embedBuilder));
    }


}
