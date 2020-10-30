package core.commands;

import core.exceptions.LastFmException;
import core.parsers.GenreParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.GenreParameters;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;
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
        GenreParameters params = parser.parse(e);
        if (params == null) {
            return;
        }
        String genre = params.getGenre();
        List<ScrobbledArtist> topInTag = e.isFromGuild()
                ? getService().getTopInTag(genre, e.getGuild().getIdLong(), 400)
                : getService().getTopInTag(genre, null, 400);

        MultipleWhoIsTagCommand.sendTopTags(e, params, genre, topInTag);
    }

}

