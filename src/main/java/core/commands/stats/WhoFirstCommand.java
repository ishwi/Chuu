package core.commands.stats;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.UserListened;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

import static core.commands.stats.WhoLastCommand.handleUserListened;

public class WhoFirstCommand extends ConcurrentCommand<ArtistParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public WhoFirstCommand(ChuuService dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
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
    protected void onCommand(MessageReceivedEvent e, @NotNull ArtistParameters params) throws LastFmException {

        String artist = params.getArtist();
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        params.setScrobbledArtist(scrobbledArtist);
        CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotify);
        List<UserListened> firsts = db.getServerFirstScrobbledArtist(scrobbledArtist.getArtistId(), e.getGuild().getIdLong());
        if (firsts.isEmpty()) {
            sendMessageQueue(e, "Couldn't get the first time this server scrobbled **" + artist + "**");
            return;
        }


        handleUserListened(e, params, firsts, true);

    }

}
