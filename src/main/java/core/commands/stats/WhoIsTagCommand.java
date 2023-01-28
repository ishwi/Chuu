package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.artists.MultipleWhoIsTagCommand;
import core.commands.utils.CommandCategory;
import core.parsers.GenreParser;
import core.parsers.Parser;
import core.parsers.params.GenreParameters;
import core.parsers.utils.Optionals;
import core.util.ServiceView;
import dao.entities.ScrobbledArtist;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WhoIsTagCommand extends ConcurrentCommand<GenreParameters> {
    public WhoIsTagCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.GENRES;
    }


    @Override
    public Parser<GenreParameters> initParser() {
        return new GenreParser(db, lastFM, Optionals.GLOBAL.opt);
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
    public void onCommand(Context e, @NotNull GenreParameters params) {


        String genre = params.getGenre();
        List<ScrobbledArtist> topInTag = e.isFromGuild() && !params.hasOptional("global")
                ? db.getTopInTag(genre, e.getGuild().getIdLong(), 400)
                : db.getTopInTag(genre, null, 400);

        MultipleWhoIsTagCommand.sendTopTags(e, params, genre, topInTag);
    }

}

