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

public class ArtistFrequencyCommand extends ResultWrappedCommand<ArtistPlays, CommandParameters> {

    public ArtistFrequencyCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
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
        pieChart.setTitle(name + "'s artists frequencies");
        return String.format("%s has %d different artists! (showing top %d)", name, count, 15);
    }


    @Override
    public ResultWrapper<ArtistPlays> getList(String[] message, MessageReceivedEvent e) {
        return getService().getArtistsFrequencies(e.getGuild().getIdLong());
    }

    @Override
    public void printList(ResultWrapper<ArtistPlays> wrapper, MessageReceivedEvent e, CommandParameters commandParameters) {
        List<ArtistPlays> list = wrapper.getResultList();
        if (list.isEmpty()) {
            sendMessageQueue(e, "No one has played any artist yet!");
        }

        List<String> collect = list.stream().map(x -> ". [" +
                                                      CommandUtil.cleanMarkdownCharacter(x.getArtistName()) +
                                                      "](" + CommandUtil.getLastFmArtistUrl(x.getArtistName()) +
                                                      ") - " + x.getCount() +
                                                      " listeners \n").collect(Collectors.toList());
        EmbedBuilder embedBuilder = initList(collect);
        embedBuilder.setTitle("Artist's frequencies");
        embedBuilder.setFooter(String.format("%s has %d different artists!%n", e.getGuild().getName(), wrapper.getRows()), null);
        embedBuilder.setThumbnail(e.getGuild().getIconUrl());
        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(collect, message1, embedBuilder));
    }

    @Override
    public String getDescription() {
        return "Artist ordered by listener";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("listeners", "frequencies", "hz");
    }

    @Override
    public String getName() {
        return "Artist Listeners";
    }
}
