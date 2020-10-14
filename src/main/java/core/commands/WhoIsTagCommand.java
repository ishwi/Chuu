package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.GenreParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.GenreParameters;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class WhoIsTagCommand extends ConcurrentCommand<GenreParameters> {
    public WhoIsTagCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.BOT_INFO;
    }

    @Override
    public Parser<GenreParameters> getParser() {
        GenreParser genreParser = new GenreParser(getService(), lastFM);
        genreParser.addOptional(new OptionalEntity("global", " show artist with the given tags from all bot users instead of only from this server"));
        return genreParser;
    }

    @Override
    public String getDescription() {
        return "Returns a list of all artists that have a given tag";
    }

    @Override
    public List<String> getAliases() {
        return List.of("whois", "who", "whotag", "whogenre", "whot", "whog");
    }

    @Override
    public String getName() {
        return "Who is x genre";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        GenreParameters parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        String genre = parse.getGenre();
        List<ScrobbledArtist> topInTag;

        if (e.isFromGuild()) {
            topInTag = getService().getTopInTag(genre, e.getGuild().getIdLong(), 400);
        } else {
            topInTag = getService().getTopInTag(genre, null, 400);

        }

        String usableServer = !e.isFromGuild() || parse.hasOptional("global") ? e.getJDA().getSelfUser().getName() : e.getGuild().getName();
        String url = !e.isFromGuild() || parse.hasOptional("global") ? e.getJDA().getSelfUser().getAvatarUrl() : e.getGuild().getIconUrl();

        if (topInTag.isEmpty()) {
            sendMessageQueue(e, usableServer + " doesnt have any artist tagged as " + genre);
            return;
        }
        StringBuilder a = new StringBuilder();

        for (int i = 0; i < 10 && i < topInTag.size(); i++) {
            a.append(i + 1).append(topInTag.get(i).toString());
        }

        String title = usableServer + "'s top tagged artist with " + genre + (":");
        MessageBuilder messageBuilder = new MessageBuilder();
        String text;
        if (CommandUtil.rand.nextInt(324) % 5 == 2) {
            text = "Use artistgenre or albumgenre for your artist or albums of the given genre";
        } else {
            text = null;
        }
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(url)
                .setFooter(text, null)
                .setTitle(title)
                .setDescription(a);
        e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build()).queue(mes ->
                new Reactionary<>(topInTag, mes, embedBuilder));
    }

}

