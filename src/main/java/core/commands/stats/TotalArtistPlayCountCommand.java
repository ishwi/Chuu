package core.commands.stats;

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

import java.util.List;
import java.util.stream.Collectors;

public class TotalArtistPlayCountCommand extends ResultWrappedCommand<ArtistPlays, CommandParameters> {

    public TotalArtistPlayCountCommand(ChuuService dao) {
        super(dao);
        this.pie = new PieableListResultWrapper<>(getParser(),
                ArtistPlays::getArtistName,
                ArtistPlays::getCount);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
    }


    @Override
    protected String fillPie(PieChart pieChart, CommandParameters params, int count) {
        String name = params.getE().getGuild().getName();
        pieChart.setTitle(name + "'s total artist plays");
        return String.format("%s has a total of %d plays (showing top %d)", name, count, 15);
    }

    @Override
    public ResultWrapper<ArtistPlays> getList(CommandParameters params) {
        return db.getServerArtistsPlays(params.getE().getGuild().getIdLong());
    }

    @Override
    public void printList(ResultWrapper<ArtistPlays> wrapper, CommandParameters params) {
        List<ArtistPlays> list = wrapper.getResultList();
        MessageReceivedEvent e = params.getE();
        if (list.isEmpty()) {
            sendMessageQueue(e, "No one has played any artist yet!");
            return;
        }

        List<String> collect = list.stream().map(x -> String.format(". [%s](%s) - %d plays %n",
                CommandUtil.cleanMarkdownCharacter(x.getArtistName()), LinkUtils.getLastFmArtistUrl(x.getArtistName()), x.getCount()))
                .collect(Collectors.toList());
        EmbedBuilder embedBuilder = initList(collect)
                .setTitle("Total artist plays")
                .setFooter(String.format("%s has %d total plays!%n", e.getGuild().getName(), wrapper.getRows()), null)
                .setThumbnail(e.getGuild().getIconUrl());
        e.getChannel().sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(collect, message1, embedBuilder));
    }


    @Override
    public String getDescription() {
        return "Total Plays";
    }

    @Override
    public List<String> getAliases() {
        return List.of("totalplays", "tp");
    }

    @Override
    public String getName() {
        return "Total Plays";
    }
}
