package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.last.ConcurrentLastFM;
import main.last.LastFMServiceException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WhoKnowsNPCommand extends MyCommandDbAccess {
	public WhoKnowsNPCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {
		String[] returned;
		try {
			returned = parse(e);
			CommandUtil.a(returned[0], getDao(), e, Boolean.valueOf(returned[1]));
		} catch (ParseException e1) {
			sendMessage(e, "You are a dumbass");
			return;
		}

	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!whoknowsnp");
	}

	@Override
	public String getDescription() {
		return "Returns list of users who know the artists you are playing right now!";
	}

	@Override
	public String getName() {
		return "Who Knows Now Playing";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("**!whoknowsnp *LastFmUser** \n" +
				"\t If useranme is not specified defaults to authors account\n" +
				"\t --image for Image format\n\n");
	}


	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {

		boolean flag = false;

		e.getChannel().sendTyping().queue();
		String[] subMessage = getSubMessage(e.getMessage());
		String[] message1 = Arrays.stream(subMessage).filter(s -> !s.equals("--image")).toArray(String[]::new);

		if (subMessage.length != message1.length) {
			flag = true;
			subMessage = message1;
		}
		String username = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
		NowPlayingArtist nowPlayingArtist = null;
		try {
			nowPlayingArtist = ConcurrentLastFM.getNowPlayingInfo(username);


		} catch (LastFMServiceException e1) {
			throw new ParseException("a", 1);
		}
		return new String[]{nowPlayingArtist.getArtistName(), Boolean.toString(flag)};

	}
}
