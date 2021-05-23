package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ResultWrappedCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.util.pie.PieableListResultWrapper;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.ArtistPlays;
import dao.entities.ResultWrapper;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.util.List;

public class TotalArtistPlayCountCommand extends ResultWrappedCommand<ArtistPlays, CommandParameters> {

    public TotalArtistPlayCountCommand(ServiceView dao) {
        super(dao);
        this.pie = new PieableListResultWrapper<>(getParser(),
                ArtistPlays::getArtistName,
                ArtistPlays::getCount);
        this.respondInPrivate = false;
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
        List<ArtistPlays> artistPlays = wrapper.getResultList();
        Context e = params.getE();
        if (artistPlays.isEmpty()) {
            sendMessageQueue(e, "No one has played any artist yet!");
            return;
        }

        List<String> lines = artistPlays.stream().map(x -> String.format(". [%s](%s) - %d plays %n",
                CommandUtil.escapeMarkdown(x.getArtistName()), LinkUtils.getLastFmArtistUrl(x.getArtistName()), x.getCount()))
                .toList();
        EmbedBuilder embedBuilder = initList(lines, e)
                .setTitle("Total artist plays")
                .setFooter(String.format("%s has %d total plays!%n", e.getGuild().getName(), wrapper.getRows()), null)
                .setThumbnail(e.getGuild().getIconUrl());
        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(lines, message1, embedBuilder));
    }


    @Override
    public String getDescription() {
        return "Most played artists in a server";
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
