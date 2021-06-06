package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.PieableListCommand;
import core.commands.utils.CommandCategory;
import core.imagerenderer.util.pie.PieableListResultWrapper;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.TagPlays;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.util.Collections;
import java.util.List;

public class ServerTagsCommand extends PieableListCommand<List<TagPlays>, CommandParameters> {
    public final PieableListResultWrapper<TagPlays, CommandParameters> pie;

    public ServerTagsCommand(ServiceView dao) {
        super(dao, true);
        this.respondInPrivate = false;
        this.pie = new PieableListResultWrapper<>(this.parser,
                TagPlays::getTag,
                TagPlays::getCount);


    }


    @Override
    public void doPie(List<TagPlays> data, CommandParameters parameters) {
        PieChart pieChart = this.pie.doPie(parameters, data);
        doPie(pieChart, parameters, data.stream().mapToInt(TagPlays::getCount).sum());
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser(new OptionalEntity("plays", "sort by scrobbles"));
    }

    @Override
    protected String fillPie(PieChart pieChart, CommandParameters params, int count) {
        String name = params.getE().getJDA().getSelfUser().getName();
        pieChart.setTitle(name + "'s artists frequencies");
        return String.format("%s has %d %s! (in top 200)", name, count, params.hasOptional("plays") ? "diff. tagged artists" : "total plays on tagged");
    }


    @Override
    public List<TagPlays> getList(CommandParameters parmas) {
        return db.getServerTags(parmas.getE().getGuild().getIdLong(), !parmas.hasOptional("plays"));
    }


    @Override
    public void printList(List<TagPlays> tags, CommandParameters params) {
        String buzzz = params.hasOptional("play") ? "tags" : "plays";
        Context e = params.getE();
        if (tags.isEmpty()) {
            sendMessageQueue(e, "No one has played any artist yet!");
            return;
        }

        List<String> lines = tags.stream().map(x ->
                String.format(". [%s](%s) - %d %s\n", LinkUtils.cleanMarkdownCharacter(x.getTag()),
                        LinkUtils.getLastFmArtistUrl(x.getTag()), x.getCount(), buzzz))
                .toList();
        EmbedBuilder embedBuilder = initList(lines, e)
                .setTitle("Server Tags")
                .setThumbnail(e.getGuild().getIconUrl());
        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(lines, message1, embedBuilder));
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
    public String slashName() {
        return "tags";
    }

    @Override
    public String getName() {
        return "Server Tags";
    }
}


