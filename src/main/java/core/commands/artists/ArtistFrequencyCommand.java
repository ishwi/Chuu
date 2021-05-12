package core.commands.artists;

import core.commands.Context;
import core.commands.abstracts.ResultWrappedCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.util.pie.PieableListResultWrapper;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.ResultWrapper;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.util.Arrays;
import java.util.List;

public class ArtistFrequencyCommand extends ResultWrappedCommand<ArtistPlays, CommandParameters> {

    public ArtistFrequencyCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
        this.pie = new PieableListResultWrapper<>(this.getParser(),
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
        String name = params.getE().getJDA().getSelfUser().getName();
        pieChart.setTitle(name + "'s artists frequencies");
        return String.format("%s has %d different artists! (showing top %d)", name, count, 15);
    }


    @Override
    public ResultWrapper<ArtistPlays> getList(CommandParameters parmas) {
        return db.getArtistsFrequencies(parmas.getE().getGuild().getIdLong());
    }

    @Override
    public void printList(ResultWrapper<ArtistPlays> wrapper, CommandParameters params) {
        List<ArtistPlays> list = wrapper.getResultList();
        Context e = params.getE();
        if (list.isEmpty()) {
            sendMessageQueue(e, "No one has played any artist yet!");
        }

        List<String> lines = list.stream().map(x -> ". [" +
                CommandUtil.cleanMarkdownCharacter(x.getArtistName()) +
                "](" + LinkUtils.getLastFmArtistUrl(x.getArtistName()) +
                ") - " + x.getCount() +
                " listeners \n").toList();
        EmbedBuilder embedBuilder = initList(lines, e)
                .setTitle("Artist's frequencies")
                .setFooter(String.format("%s has %d different artists!%n", e.getGuild().getName(), wrapper.getRows()), null)
                .setThumbnail(e.getGuild().getIconUrl());
        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(lines, message1, embedBuilder));
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
