package core.commands.artists;

import core.commands.abstracts.ResultWrappedCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.util.pie.PieableListResultWrapper;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.ResultWrapper;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.Arrays;
import java.util.List;

public class GlobalArtistFrequenciesCommand extends ResultWrappedCommand<ArtistPlays, CommandParameters> {
    public GlobalArtistFrequenciesCommand(ChuuService dao) {
        super(dao);
        this.pie = new PieableListResultWrapper<>(initParser(),
                ArtistPlays::getArtistName,
                ArtistPlays::getCount);
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
    protected String fillPie(PieChart pieChart, CommandParameters params, int count) {
        String name = params.getE().getJDA().getSelfUser().getName();
        pieChart.setTitle(name + "'s most popular artists");
        return String.format("%s has %d different artists! (showing top %d)", name, count, 15);
    }


    @Override
    public ResultWrapper<ArtistPlays> getList(CommandParameters parmas) {
        return db.getArtistsFrequenciesGlobal();
    }

    @Override
    public void printList(ResultWrapper<ArtistPlays> list, CommandParameters params) {
        MessageReceivedEvent e = params.getE();
        if (list.getRows() == 0) {
            sendMessageQueue(e, "No one has ever played any artist yet!");
        }

        StringBuilder a = new StringBuilder();
        List<ArtistPlays> resultList = list.getResultList();

        List<String> lines = resultList.stream().map(x -> String.format(". [%s](%s) - %d total listeners%n", CommandUtil.cleanMarkdownCharacter(x.getArtistName()),
                LinkUtils.getLastFmArtistUrl(x.getArtistName()),
                x.getCount())).toList();
        for (int i = 0, size = lines.size(); i < 10 && i < size; i++) {
            String text = lines.get(i);
            a.append(i + 1).append(text);
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setDescription(a)
                .setTitle("Most Popular artists")
                .setColor(ColorService.computeColor(e))
                .setFooter(String.format("%s has %d different artists!%n", e.getJDA().getSelfUser().getName(), list.getRows()), null)
                .setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
        e.getChannel().sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(lines, message1, embedBuilder));
    }

    @Override
    public String getDescription() {
        return " Artists ranked by listeners on all servers that this bot handles";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("globallisteners", "globalhz", "gl");
    }

    @Override
    public String getName() {
        return "Total Listeners";
    }

}
