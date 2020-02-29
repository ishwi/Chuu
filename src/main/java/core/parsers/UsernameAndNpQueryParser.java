package core.parsers;

import core.apis.last.ConcurrentLastFM;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UsernameAndNpQueryParser extends ArtistParser {
    public UsernameAndNpQueryParser(ChuuService dao, ConcurrentLastFM lastFM) {
        super(dao, lastFM);
    }

    @Override
    public String[] doSomethingWithNp(NowPlayingArtist np, User sample, MessageReceivedEvent e) {
        return new String[]{np.getArtistName() + " " + np.getSongName(), String.valueOf(e.getAuthor().getIdLong())};
    }

    @Override
    public String getUsageLogic(String commandName) {
		return "**" + commandName + " [whatever you want to search for] **\n" +
               "\tIf you don't introduce a query it takes your now playing song\n" +
               "\tYou can add an username to use their now playing song\n";
	}
}
