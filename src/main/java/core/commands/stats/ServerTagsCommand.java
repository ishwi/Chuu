package core.commands.stats;

import core.commands.abstracts.PieableListCommand;
import core.commands.utils.CommandCategory;
import core.imagerenderer.util.PieableListResultWrapper;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.TagPlays;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ServerTagsCommand extends PieableListCommand<List<TagPlays>, CommandParameters> {
    public final PieableListResultWrapper<TagPlays, CommandParameters> pie;

    public ServerTagsCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
        isLongRunningCommand = true;
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
        NoOpParser noOpParser = new NoOpParser();
        noOpParser.addOptional(new OptionalEntity("plays", "to display number of artists instead of scrobbles"));
        return noOpParser;
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
    public void printList(List<TagPlays> list, CommandParameters params) {
        String buzzz = params.hasOptional("play") ? "tags" : "plays";
        MessageReceivedEvent e = params.getE();
        if (list.isEmpty()) {
            sendMessageQueue(e, "No one has played any artist yet!");
        }

        List<String> collect = list.stream().map(x ->
                String.format(". [%s](%s) - %d %s\n", LinkUtils.cleanMarkdownCharacter(x.getTag()),
                        LinkUtils.getLastFmArtistUrl(x.getTag()), x.getCount(), buzzz))
                .collect(Collectors.toList());
        EmbedBuilder embedBuilder = initList(collect, e)
                .setTitle("Server Tags")
                .setThumbnail(e.getGuild().getIconUrl());
        e.getChannel().sendMessage(embedBuilder.build()).queue(message1 ->
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


