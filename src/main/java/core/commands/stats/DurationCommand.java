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
import core.util.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.Track;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class DurationCommand extends ConcurrentCommand<ChuuDataParams> {
    public DurationCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db).addOptional(new OptionalEntity("shortest", "show the shortest songs first"));
    }

    @Override
    public String getDescription() {
        return "Shows your songs with the most or least length";
    }

    @Override
    public List<String> getAliases() {
        return List.of("duration", "length");
    }

    @Override
    public String getName() {
        return "Duration";
    }

    @Override
    public void onCommand(Context e, @NotNull ChuuDataParams params) {
        LastFMData data = params.getLastFMData();
        List<Track> tracks = db.getUserTrackByLength(data.getName(), !params.hasOptional("shortest"));
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, params.getLastFMData().getDiscordId());
        if (tracks.isEmpty()) {
            e.sendMessage(uInfo.username() + " doesnt have any song with length data").queue();
            return;
        }
        Function<Track, String> popularity = (s) -> {
            if (s.getDuration() == 0) {
                return "No data";
            }
            return CommandUtil.msToString(s.getDuration() * 1000L);
        };
        List<String> strs = tracks.stream().map(t ->
                String.format(". **[%s - %s](%s)** - %s (%d %s)%n",
                        LinkUtils.cleanMarkdownCharacter(t.getName()),
                        LinkUtils.cleanMarkdownCharacter(t.getArtist()), PrivacyUtils.getLastFmArtistTrackUserUrl(t.getArtist(), t.getName(), params.getLastFMData().getName()),
                        popularity.apply(t),
                        t.getPlays(), CommandUtil.singlePlural(t.getPlays(), "play", "plays"))).toList();


        String title = uInfo.username() + "'s tracks with duration";
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setAuthor(title, PrivacyUtils.getLastFmUser(params.getLastFMData().getName()), uInfo.urlImage())
                .setFooter("Showing top %s songs".formatted(tracks.size()));

        new PaginatorBuilder<>(e, embedBuilder, strs).build().queue();
    }
}
