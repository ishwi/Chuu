package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.params.ArtistParameters;
import core.services.NPService;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class ArtistParser extends DaoParser<ArtistParameters> {
    final ConcurrentLastFM lastFM;


    public ArtistParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... strings) {
        super(dao);
        this.lastFM = lastFM;
        opts.addAll(Arrays.asList(strings));
    }

    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("noredirect", "not change the artist name for a correction automatically"));
    }

    @Override
    protected ArtistParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        ParserAux parserAux = new ParserAux(words);
        User oneUser = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();

        LastFMData lastFMData = findLastfmFromID(oneUser, e);
        if (words.length == 0) {
            NowPlayingArtist np;
            if (isAllowUnaothorizedUsers() && lastFMData.getName() == null) {
                throw new InstanceNotFoundException(oneUser.getIdLong());
            }
            try {
                LastFMData lastfmFromID = findLastfmFromID(e.getAuthor(), e);
                np = new NPService(lastFM, lastfmFromID).getNowPlaying();
            } catch (InstanceNotFoundException ex) {
                np = new NPService(lastFM, lastFMData).getNowPlaying();
            }
            return new ArtistParameters(e, np.getArtistName(), lastFMData);
        } else {
            return new ArtistParameters(e, String.join(" ", words), lastFMData);
        }
    }

    @Override
    public String getUsageLogic(String commandName) {

        return "**" + commandName + " *artist* *username***\n" +
                "\tIf an username it's not provided it defaults to authors account, only ping, tag format (user#number),discord id, u:username or lfm:lastfmname\n";

    }


}
