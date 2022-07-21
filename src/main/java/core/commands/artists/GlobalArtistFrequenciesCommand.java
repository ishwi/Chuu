package core.commands.artists;

import core.commands.Context;
import core.commands.abstracts.ResultWrappedCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.util.pie.PieableListResultWrapper;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;
import dao.entities.ArtistPlays;
import dao.entities.ResultWrapper;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class GlobalArtistFrequenciesCommand extends ResultWrappedCommand<ArtistPlays, CommandParameters> {
    public GlobalArtistFrequenciesCommand(ServiceView dao) {
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
        return new NoOpParser();
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
        Context e = params.getE();
        if (list.getRows() == 0) {
            sendMessageQueue(e, "No one has ever played any artist yet!");
        }

        List<ArtistPlays> artistFrequencies = list.getResultList();


        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setTitle("Most Popular artists")
                .setFooter(String.format("%s has %d different artists!%n", e.getJDA().getSelfUser().getName(), list.getRows()), null)
                .setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());

        Function<ArtistPlays, String> mapper = x -> String.format(". [%s](%s) - %d total listeners%n", CommandUtil.escapeMarkdown(x.getArtistName()),
                LinkUtils.getLastFmArtistUrl(x.getArtistName()),
                x.getCount());

        new PaginatorBuilder<>(e, embedBuilder, artistFrequencies).mapper(mapper).build().queue();
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
    public String slashName() {
        return "listeners";
    }

    @Override
    public String getName() {
        return "Total Listeners";
    }

}
