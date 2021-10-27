package core.commands.moderation;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.scheduledtasks.ArtistMbidUpdater;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;

import javax.annotation.Nonnull;
import java.util.List;

public class MbidUpdatedCommand extends ConcurrentCommand<ChuuDataParams> {

    public MbidUpdatedCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
    }

    @Override
    public String getDescription() {
        return "Updates the mbid of all artist of an user";
    }

    @Override
    public List<String> getAliases() {
        return List.of("mbidupdater");
    }

    @Override
    public String getName() {
        return "MBID updater";
    }


    @Override
    public void onCommand(Context e, @Nonnull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {
        long issuer = e.getAuthor().getIdLong();
        LastFMData lastFMData = db.findLastFMData(issuer);
        if (lastFMData.getRole() != Role.ADMIN) {
            sendMessageQueue(e, "Bot admins only :)");
            return;
        }
        List<ScrobbledArtist> scrobbledArtists = new ArtistMbidUpdater(db, lastFM).updateAndGet(params.getLastFMData());
        if (scrobbledArtists == null) {
            sendMessageQueue(e, "Something didnt go as planned while fetching artist mbids");
        } else {
            sendMessageQueue(e, "Refreshed %d artists".formatted(scrobbledArtists.size()));
        }
    }
}
