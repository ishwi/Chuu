package core.commands;

import core.commands.util.PieableResultWrapper;
import core.otherlisteners.Reactionary;
import core.parsers.params.CommandParameters;
import core.parsers.params.OptionalParameter;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.ResultWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalTotalArtistPlaysCountCommand extends ResultWrappedCommand<ArtistPlays, CommandParameters> {
    public GlobalTotalArtistPlaysCountCommand(ChuuService dao) {
        super(dao);
        this.pie = new PieableResultWrapper<>(
                ArtistPlays::getArtistName,
                ArtistPlays::getCount);
    }

    @Override
    public CommandParameters getParameters(MessageReceivedEvent e, String[] message) {
        return new CommandParameters(message, e, new OptionalParameter("--pie", 0));

    }

    @Override
    protected String fillPie(PieChart pieChart, CommandParameters params, int count) {
        String name = params.getE().getJDA().getSelfUser().getName();
        pieChart.setTitle(name + "'s most played artists");
        return String.format("%s has %d total plays! (showing top %d)", name, count, 15);
    }

    @Override
    public ResultWrapper<ArtistPlays> getList(String[] message, MessageReceivedEvent e) {
        return getService().getArtistPlayCountGlobal();
    }

    @Override
    public void printList(ResultWrapper<ArtistPlays> list, MessageReceivedEvent e, CommandParameters commandParameters) {
        if (list.getRows() == 0) {
            sendMessageQueue(e, "No one has ever played any artist!");
            return;
        }

        List<ArtistPlays> resultList = list.getResultList();

        List<String> collect = resultList.stream().map(x -> String.format(". [%s](%s) - %d plays%n",
                CommandUtil.cleanMarkdownCharacter(x.getArtistName()), CommandUtil.getLastFmArtistUrl(x.getArtistName()), x.getCount())).collect(Collectors.toList());
        EmbedBuilder embedBuilder = initList(collect)
                .setTitle("Most Played Artists")
                .setFooter(String.format("%s has stored %d plays!%n", e.getJDA().getSelfUser().getName(), list.getRows()), null)
                .setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(collect, message1, embedBuilder));
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
