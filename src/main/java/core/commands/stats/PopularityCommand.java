package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.utils.OptionalEntity;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.ScrobbledTrack;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class PopularityCommand extends ConcurrentCommand<ChuuDataParams> {
    private static final DecimalFormat formatter = new DecimalFormat("#0.#");

    public PopularityCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        OnlyUsernameParser parser = new OnlyUsernameParser(db);
        parser.addOptional(new OptionalEntity("sort", "sort by popularity"));
        parser.addOptional(new OptionalEntity("reverse", "sort by less popularity"));
        return parser;
    }

    @Override
    public String getDescription() {
        return "Songs ordered by popularity";
    }

    @Override
    public List<String> getAliases() {
        return List.of("popularity", "popular");
    }

    @Override
    public String getName() {
        return "Popularity";
    }

    @Override
    protected void onCommand(Context e, @Nonnull ChuuDataParams params) {
        String name = params.getLastFMData().getName();
        List<ScrobbledTrack> topTracks = db.getTopTracks(name, 2000);
        if (params.hasOptional("sort")) {
            topTracks = topTracks.stream().sorted(Comparator.comparingInt(ScrobbledTrack::getPopularity).reversed()).toList();
        } else if (params.hasOptional("reverse")) {
            topTracks = topTracks.stream().sorted(Comparator.comparingInt(scrobbledTrack -> {
                int popularity = scrobbledTrack.getPopularity();
                if (popularity == 0) {
                    return Integer.MAX_VALUE;
                }
                return popularity;
            })).toList();
        }
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, params.getLastFMData().getDiscordId());
        if (topTracks.isEmpty()) {
            e.sendMessage(uInfo.username() + " doesnt have any song.").queue();
            return;
        }
        Function<ScrobbledTrack, String> popularity = (s) -> {
            if (s.getPopularity() == 0) {
                return "No data";
            }
            return s.getPopularity() + "%";
        };
        List<String> strs = topTracks.stream().map(t ->
                String.format(". **[%s - %s](%s)** - %s (%d %s)%n",
                        LinkUtils.cleanMarkdownCharacter(t.getName()),
                        LinkUtils.cleanMarkdownCharacter(t.getArtist()), PrivacyUtils.getLastFmArtistTrackUserUrl(t.getArtist(), t.getName(), params.getLastFMData().getName()),
                        popularity.apply(t),
                        t.getCount(), CommandUtil.singlePlural(t.getCount(), "play", "plays"))).toList();


        String title = uInfo.username() + "'s tracks";
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setAuthor(title, PrivacyUtils.getLastFmUser(params.getLastFMData().getName()), uInfo.urlImage())
                .setFooter("Showing top %s songs".formatted(topTracks.size()));

        new PaginatorBuilder<>(e, embedBuilder, strs).build().queue();


    }


}
