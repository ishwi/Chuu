package core.commands.artists;

import core.commands.abstracts.ResultWrappedCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.util.PieableListResultWrapper;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.ResultWrapper;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalTotalArtistPlaysCountCommand extends ResultWrappedCommand<ArtistPlays, CommandParameters> {
    public GlobalTotalArtistPlaysCountCommand(ChuuService dao) {
        super(dao);
        this.pie = new PieableListResultWrapper<>(getParser(),
                ArtistPlays::getArtistName,
                ArtistPlays::getCount);
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
    protected String fillPie(PieChart pieChart, CommandParameters params, int count) {
        String name = params.getE().getJDA().getSelfUser().getName();
        pieChart.setTitle(name + "'s most played artists");
        return String.format("%s has %d total plays! (showing top %d)", name, count, 15);
    }

    @Override
    public ResultWrapper<ArtistPlays> getList(CommandParameters params) {
        return db.getArtistPlayCountGlobal();
    }

    @Override
    public void printList(ResultWrapper<ArtistPlays> list, CommandParameters params) {
        MessageReceivedEvent e = params.getE();
        if (list.getRows() == 0) {
            sendMessageQueue(e, "No one has ever played any artist!");
            return;
        }

        List<ArtistPlays> resultList = list.getResultList();

        List<String> strList = resultList.stream().map(x -> String.format(". [%s](%s) - %d plays%n",
                CommandUtil.cleanMarkdownCharacter(x.getArtistName()), LinkUtils.getLastFmArtistUrl(x.getArtistName()), x.getCount())).collect(Collectors.toList());
        EmbedBuilder embedBuilder = initList(strList)
                .setTitle("Most Played Artists")
                .setFooter(String.format("%s has stored %d plays!%n", e.getJDA().getSelfUser().getName(), list.getRows()), null)
                .setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
        e.getChannel().sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(strList, message1, embedBuilder));
    }

    @Override
    public String getDescription() {
        return " Artists ranked by total plays on all servers that this bot handles";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("globalplays", "gp");
    }

    @Override
    public String getName() {
        return "Total Artist Plays";
    }
}
