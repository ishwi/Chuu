package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.ScrobbledArtist;
import dao.entities.UserListened;

import javax.annotation.Nonnull;
import java.util.List;

import static core.commands.stats.WhoLastCommand.handleUserListened;

public class WhoFirstCommand extends ConcurrentCommand<ArtistParameters> {

    public WhoFirstCommand(ServiceView dao) {
        super(dao, true);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_LEADERBOARDS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Who listened first to an artist on a server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("whofirst", "wf", "wfirst", "whof");
    }

    @Override
    public String getName() {
        return "Who listened first";
    }


    @Override
    public void onCommand(Context e, @Nonnull ArtistParameters params) throws LastFmException {


        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), !params.isNoredirect());
        params.setScrobbledArtist(sA);

        List<UserListened> firsts = db.getServerFirstScrobbledArtist(sA.getArtistId(), e.getGuild().getIdLong());
        if (firsts.isEmpty()) {
            sendMessageQueue(e, "Couldn't get the first time this server scrobbled **" + sA.getArtist() + "**");
            return;
        }


        handleUserListened(e, params, firsts, true);

    }

}
