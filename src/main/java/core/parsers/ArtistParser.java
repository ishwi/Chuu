package core.parsers;

import core.apis.last.ConcurrentLastFM;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class ArtistParser extends ArtistAlbumParser {

    public ArtistParser(ChuuService dao, ConcurrentLastFM lastFM) {
        super(dao, lastFM);
    }

    public ArtistParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... strings) {
        super(dao, lastFM);
        opts.addAll(Arrays.asList(strings));
    }

    @Override
    String[] doSomethingWithNp(NowPlayingArtist np, User sample, MessageReceivedEvent e) {
        //With the ping you get only the np from that person
        return new String[]{np.getArtistName(), String.valueOf(e.getAuthor().getIdLong())};
    }

	@Override
	String[] doSomethingWithString(String[] subMessage, User sample, MessageReceivedEvent e) {
		//With the ping you get the stringed artsit and the user who you pongedf
		return new String[]{artistMultipleWords(subMessage), String.valueOf(sample.getIdLong())};
	}

	@Override
	public String getUsageLogic(String commandName) {

		return "**" + commandName + " *artist*** \n";

	}


}
