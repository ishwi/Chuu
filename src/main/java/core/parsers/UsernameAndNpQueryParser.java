package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.params.ExtraParameters;
import core.parsers.params.WordParameter;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UsernameAndNpQueryParser extends DaoParser<ExtraParameters<WordParameter, User>> {

    private final ConcurrentLastFM lastFM;

    public UsernameAndNpQueryParser(ChuuService dao, ConcurrentLastFM lastFM) {
        super(dao);
        this.lastFM = lastFM;
    }

    @Override
    protected ExtraParameters<WordParameter, User> parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        ParserAux parserAux = new ParserAux(words);
        User oneUser = parserAux.getOneUser(e);
        words = parserAux.getMessage();

        if (words.length == 0) {
            NowPlayingArtist np;
            String userName = dao.findLastFMData(oneUser.getIdLong()).getName();
            np = lastFM.getNowPlayingInfo(userName);
            return new ExtraParameters<>(e, new WordParameter(e, np.getArtistName() + " " + np.getSongName()), oneUser);
        } else {
            return new ExtraParameters<>(e, new WordParameter(e, String.join(" ", words)), oneUser);
        }
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " [whatever you want to search for] **\n" +
                "\tIf you don't introduce a query it takes your now playing song\n" +
                "\tYou can add an username with only ping and tag format (user#number) to use their now playing song\n";
    }
}
