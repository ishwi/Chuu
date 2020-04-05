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

public class GlobalArtistFrequenciesCommand extends ResultWrappedCommand<ArtistPlays, CommandParameters> {
    public GlobalArtistFrequenciesCommand(ChuuService dao) {
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
        pieChart.setTitle(name + "'s most popular artists");
        return String.format("%s has %d different artists! (showing top %d)", name, count, 15);
    }


    @Override
    public ResultWrapper<ArtistPlays> getList(String[] message, MessageReceivedEvent e) {
        return getService().getArtistsFrequenciesGlobal();
    }

    @Override
    public void printList(ResultWrapper<ArtistPlays> list, MessageReceivedEvent e, CommandParameters commandParameters) {
        if (list.getRows() == 0) {
            sendMessageQueue(e, "No one has ever played any artist yet!");
        }

        StringBuilder a = new StringBuilder();
        List<ArtistPlays> resultList = list.getResultList();

        List<String> collect = resultList.stream().map(x -> String.format(". [%s](%s) - %d total listeners\n", CommandUtil.cleanMarkdownCharacter(x.getArtistName()),
                CommandUtil.getLastFmArtistUrl(x.getArtistName()),
                x.getCount())).collect(Collectors.toList());
        for (int i = 0, size = collect.size(); i < 10 && i < size; i++) {
            String text = collect.get(i);
            a.append(i + 1).append(text);
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setDescription(a)
                .setTitle("Most Popular artists")
                .setColor(CommandUtil.randomColor())
                .setFooter(String.format("%s has %d different artists!\n", e.getJDA().getSelfUser().getName(), list.getRows()), null)
                .setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                executor.execute(() -> new Reactionary<>(collect, message1, embedBuilder)));
    }

    @Override
    public String getDescription() {
        return " Artists ranked by listeners on all servers that this bot handles";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("globalListeners", "globalhz", "gl");
    }

    @Override
    public String getName() {
        return "Total Listeners";
    }

}
