package core.commands;

import core.imagerenderer.util.PieableListResultWrapper;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.ResultWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ServerTags extends ResultWrappedCommand<ArtistPlays, CommandParameters> {

    public ServerTags(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
        this.pie = new PieableListResultWrapper<>(this.parser,
                ArtistPlays::getArtistName,
                ArtistPlays::getCount);


    }


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<CommandParameters> getParser() {
        NoOpParser noOpParser = new NoOpParser();
        noOpParser.addOptional(new OptionalEntity("count", "to display number of artists instead of scrobbles"));
        return noOpParser;
    }

    @Override
    protected String fillPie(PieChart pieChart, CommandParameters params, int count) {
        String name = params.getE().getJDA().getSelfUser().getName();
        pieChart.setTitle(name + "'s artists frequencies");
        return String.format("%s has %d different artists! (showing top %d)", name, count, 15);
    }


    @Override
    public ResultWrapper<ArtistPlays> getList(CommandParameters parmas) {
        return getService().getServerTags(parmas.getE().getGuild().getIdLong(), parmas.hasOptional("count"));
    }

    @Override
    public void printList(ResultWrapper<ArtistPlays> wrapper, CommandParameters params) {
        List<ArtistPlays> list = wrapper.getResultList();
        String buzzz = params.hasOptional("count") ? "diff. artists" : "total plays";
        MessageReceivedEvent e = params.getE();
        if (list.isEmpty()) {
            sendMessageQueue(e, "No one has played any artist yet!");
        }

        List<String> collect = list.stream().map(x -> ". [" +
                CommandUtil.cleanMarkdownCharacter(x.getArtistName()) +
                "](" + CommandUtil.getLastFmTagUrl(x.getArtistName()) +
                ") - " + x.getCount() + " " +
                buzzz + "\n").collect(Collectors.toList());
        EmbedBuilder embedBuilder = initList(collect);
        embedBuilder.setTitle("Server Tags");
        embedBuilder.setFooter(String.format("%s has %d different tags!%n", e.getGuild().getName(), wrapper.getRows()), null);
        embedBuilder.setThumbnail(e.getGuild().getIconUrl());
        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(collect, message1, embedBuilder));
    }

    @Override
    public String getDescription() {
        return "Top Tags within a server";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("servertags");
    }

    @Override
    public String getName() {
        return "Server Tags";
    }
}
