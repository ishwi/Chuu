package core.commands.artists;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.MultipleGenresParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.MultipleGenresParameters;
import core.parsers.utils.Optionals;
import core.util.ServiceView;
import dao.entities.ScrobbledArtist;
import dao.entities.SearchMode;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MultipleWhoIsTagCommand extends ConcurrentCommand<MultipleGenresParameters> {
    public MultipleWhoIsTagCommand(ServiceView dao) {
        super(dao);
    }

    public static void sendTopTags(Context e, CommandParameters params, String genre, List<ScrobbledArtist> topInTag) {
        String usableServer = !e.isFromGuild() || params.hasOptional("global") ? e.getJDA().getSelfUser().getName() : e.getGuild().getName();

        if (topInTag.isEmpty()) {
            e.sendMessage(usableServer + " doesnt have any artist tagged as " + genre).queue();
            return;
        }

        String url = !e.isFromGuild() || params.hasOptional("global") ? e.getJDA().getSelfUser().getAvatarUrl() : e.getGuild().getIconUrl();
        String text = CommandUtil.rand.nextInt(324) % 5 == 2 ? "Use artistgenre or albumgenre for your artist or albums of the given genre" : null;

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setThumbnail(url)
                .setFooter(text, null)
                .setTitle(usableServer + "'s top tagged artist with " + genre + (":"));

        new PaginatorBuilder<>(e, embedBuilder, topInTag).build().queue();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.GENRES;
    }

    @Override
    public Parser<MultipleGenresParameters> initParser() {
        MultipleGenresParser genreParser = new MultipleGenresParser(db, lastFM);
        genreParser.addOptional(Optionals.GLOBAL.opt);
        return genreParser;
    }

    @Override
    public String getDescription() {
        return "Returns a list of all artists that have a given tag";
    }

    @Override
    public List<String> getAliases() {
        return List.of("multiwhois", "multiwho", "mutliwhotag", "multiwhogenre", "multiwhot", "multiwhog", "mwhois", "mwho", "mwhotag", "mwhogenre", "mwhot", "mwhog", "whomstve", "whos");
    }

    @Override
    public String getName() {
        return "Who is a set of genres";
    }

    @Override
    public void onCommand(Context e, @NotNull MultipleGenresParameters params) {


        Set<String> genres = params.getGenres();
        List<ScrobbledArtist> topInTag;
        String genre = genres.stream().map(WordUtils::capitalizeFully).collect(Collectors.joining(params.getMode() == SearchMode.EXCLUSIVE ? ", " : "| "));

        topInTag = e.isFromGuild()
                ? db.getTopInTag(genres, e.getGuild().getIdLong(), 400, params.getMode())
                : db.getTopInTag(genres, null, 400, params.getMode());
        sendTopTags(e, params, genre, topInTag);
    }
}
