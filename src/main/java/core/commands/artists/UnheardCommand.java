package core.commands.artists;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ArtistParser;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.params.NumberParameters;
import core.parsers.utils.OptionalEntity;
import core.services.validators.ArtistValidator;
import core.util.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.ScrobbledArtist;
import dao.entities.UnheardCount;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class UnheardCommand extends ConcurrentCommand<NumberParameters<ArtistParameters>> {

    public UnheardCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<NumberParameters<ArtistParameters>> initParser() {

        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can introduce a goal that will be the number of scrobbles that you want to obtain";
        return new NumberParser<>(new ArtistParser(db, lastFM).addOptional(new OptionalEntity("listeners", "sort the list by listener count, not scrobbles")),
                null, Long.MAX_VALUE,
                map, s, false, true, true, "goal");

    }

    @Override
    public String getDescription() {
        return "What songs you haven't heard for an specific artist";
    }

    @Override
    public List<String> getAliases() {
        return List.of("unexplored", "unheard", "notlistened", "tolisten");
    }

    @Override
    public String getName() {
        return "Unexplored";
    }


    @Override
    public void onCommand(Context e, @NotNull NumberParameters<ArtistParameters> numberParameters) throws LastFmException, InstanceNotFoundException {
        ArtistParameters params = numberParameters.getInnerParams();
        long userId = params.getLastFMData().getDiscordId();

        ScrobbledArtist who = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), !params.isNoredirect());
        String lastFmName = params.getLastFMData().getName();
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, userId);
        String userString = uInfo.username();

        Long extraParam = numberParameters.getExtraParam();
        List<UnheardCount> unheardSongs = db.getUnheardSongs(lastFmName, who.getArtistId(), numberParameters.hasOptional("listeners"), extraParam);
        String artist = who.getArtist();
        String extraText = "";
        if (extraParam != null) {
            extraText = " with more than %d plays".formatted(extraParam);
        }
        if (unheardSongs.isEmpty()) {
            sendMessageQueue(e, "%s has listened to all **%s** tracks%s".formatted(userString, CommandUtil.escapeMarkdown(artist), extraText));
            return;
        }

        String title = "%s's %s unheard tracks%s".formatted(userString, artist, extraText);
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setAuthor(title, PrivacyUtils.getLastFmArtistUserUrl(artist, lastFmName), uInfo.urlImage())
                .setThumbnail(CommandUtil.noImageUrl(who.getUrl())).setFooter("%d %s %s yet to be listened%s".formatted(unheardSongs.size(), artist,
                        CommandUtil.singlePlural(unheardSongs.size(), "track", "tracks"), extraText));

        new PaginatorBuilder<>(e, embedBuilder, unheardSongs).mapper(g -> ". **[%s](%s)** - %d %s (%s %s)\n".formatted(CommandUtil.escapeMarkdown(g.name()),
                LinkUtils.getLastFMArtistTrack(artist, g.name()),
                g.scrobbles(),
                CommandUtil.singlePlural(g.scrobbles(), "play", "plays"),
                g.listeners(),
                CommandUtil.singlePlural(g.listeners(), "listener", "listeners"))).build().queue();
    }
}
