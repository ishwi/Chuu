package core.commands;

import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.MultipleGenresParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.MultipleGenresParameters;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.SearchMode;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.text.WordUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MultipleWhoIsTagCommand extends ConcurrentCommand<MultipleGenresParameters> {
    public MultipleWhoIsTagCommand(ChuuService dao) {
        super(dao);
    }

    public static void sendTopTags(MessageReceivedEvent e, CommandParameters params, String genre, List<ScrobbledArtist> topInTag) {
        String usableServer = !e.isFromGuild() || params.hasOptional("global") ? e.getJDA().getSelfUser().getName() : e.getGuild().getName();
        String url = !e.isFromGuild() || params.hasOptional("global") ? e.getJDA().getSelfUser().getAvatarUrl() : e.getGuild().getIconUrl();

        if (topInTag.isEmpty()) {
            e.getChannel().sendMessage(usableServer + " doesnt have any artist tagged as " + genre).queue();
            return;
        }
        StringBuilder a = new StringBuilder();

        for (int i = 0; i < 10 && i < topInTag.size(); i++) {
            a.append(i + 1).append(topInTag.get(i).toString());
        }

        String title = usableServer + "'s top tagged artist with " + genre + (":");
        MessageBuilder messageBuilder = new MessageBuilder();
        String text = CommandUtil.rand.nextInt(324) % 5 == 2 ? "Use artistgenre or albumgenre for your artist or albums of the given genre" : null;
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(url)
                .setFooter(text, null)
                .setTitle(title)
                .setDescription(a);
        e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build()).queue(mes ->
                new Reactionary<>(topInTag, mes, embedBuilder));
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_INFO;
    }

    @Override
    public Parser<MultipleGenresParameters> initParser() {
        MultipleGenresParser genreParser = new MultipleGenresParser(getService(), lastFM);
        genreParser.addOptional(new OptionalEntity("global", " show artist with the given tags from all bot users instead of only from this server"));
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        MultipleGenresParameters params = parser.parse(e);
        if (params == null) {
            return;
        }
        Set<String> genres = params.getGenres();
        List<ScrobbledArtist> topInTag;
        String genre = genres.stream().map(WordUtils::capitalizeFully).collect(Collectors.joining(params.getMode() == SearchMode.EXCLUSIVE ? ", " : "| "));

        topInTag = e.isFromGuild()
                ? getService().getTopInTag(genres, e.getGuild().getIdLong(), 400, params.getMode())
                : getService().getTopInTag(genres, null, 400, params.getMode());
        sendTopTags(e, params, genre, topInTag);
    }
}
