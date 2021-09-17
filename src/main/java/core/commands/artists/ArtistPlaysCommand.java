package core.commands.artists;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.ArtistSummary;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;

import javax.annotation.Nonnull;
import java.util.List;

public class ArtistPlaysCommand extends ConcurrentCommand<ArtistParameters> {
    public ArtistPlaysCommand(ServiceView dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Gets the amount of times an user has played an specific artist";
    }

    @Override
    public List<String> getAliases() {
        return List.of("plays", "p");
    }

    @Override
    public String getName() {
        return "Plays on a specific artist";
    }

    @Override
    protected void onCommand(Context e, @Nonnull ArtistParameters params) throws LastFmException {

        ScrobbledArtist scrobbledArtist = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), false, !params.isNoredirect());
        long whom = params.getLastFMData().getDiscordId();
        int a;
        LastFMData data = params.getLastFMData();
        ArtistSummary artistSummary = lastFM.getArtistSummary(scrobbledArtist.getArtist(), data);
        a = artistSummary.userPlayCount();
        String usernameString = getUserString(e, whom, data.getName());
        String ending = a != 1 ? "times" : "time";
        sendMessageQueue(e, "**" + usernameString + "** has scrobbled **" + CommandUtil.escapeMarkdown(artistSummary.artistname()) + " " + a + "** " + ending);

    }
}
