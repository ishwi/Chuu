package core.commands.stats;

import core.apis.bandcamp.BandcampApi;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.GreedyStringParser;
import core.parsers.Parser;
import core.parsers.params.StringParameters;
import core.util.ServiceView;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BandcampReleases extends ConcurrentCommand<StringParameters> {
    public BandcampReleases(ServiceView dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.GENRES;
    }

    @Override
    public Parser<StringParameters> initParser() {
        return new GreedyStringParser();
    }

    @Override
    public String getDescription() {
        return "bandcamp";
    }

    @Override
    public List<String> getAliases() {
        return List.of("bandcamp");
    }

    @Override
    public String getName() {
        return "bandcamp";
    }

    @Override
    public void onCommand(Context e, @NotNull StringParameters params) {

        String[] genres = params.getValue().split("\\s+");
        Set<String> unique = new HashSet<>(List.of(genres));
        BandcampApi bandcampApi = new BandcampApi();
        List<BandcampApi.Result> result = bandcampApi.discoverReleases(unique.stream().toList());


        if (result.isEmpty()) {
            e.sendMessage("Not found").queue();
            return;
        }

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setFooter(String.join(" • ", genres), null)
                .setTitle("New bandcamp releases");

        new PaginatorBuilder<>(e, embedBuilder, result)
                .mapper(res -> String.format(". **[%s - %s](%s)**%n",
                        CommandUtil.escapeMarkdown(res.artistName())
                        , CommandUtil.escapeMarkdown(res.title()), res.url())).build().queue();
    }
}
