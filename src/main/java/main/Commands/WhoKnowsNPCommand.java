package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmException;
import main.Exceptions.ParseException;
import main.Youtube.DiscogsApi;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WhoKnowsNPCommand extends WhoKnowsCommand {


	public WhoKnowsNPCommand(DaoImplementation dao, DiscogsApi discogsApi) {
		super(dao, discogsApi);
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
		return Collections.singletonList("!whoknowsnp *LastFmUser \n" +
				"\t If useranme is not specified defaults to authors account\n" +
				"\t --image for Image format\n\n");
	}


	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {

		boolean flag = false;

		String[] subMessage = getSubMessage(e.getMessage());
		String[] message1 = Arrays.stream(subMessage).filter(s -> !s.equals("--image")).toArray(String[]::new);

		if (subMessage.length != message1.length) {
			flag = true;
			subMessage = message1;
		}
		String username = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
		NowPlayingArtist nowPlayingArtist;
		try {
			nowPlayingArtist = lastFM.getNowPlayingInfo(username);


		} catch (LastFMNoPlaysException e1) {
			throw new ParseException("NoPlays");

		} catch (LastFmException e1) {
			throw new ParseException("LastFM");
		}
		return new String[]{nowPlayingArtist.getArtistName(), Boolean.toString(flag)};

	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String message;
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request: ";

		switch (code) {
			case 1:
				message = "User was not found on the database, register first!";
				break;
			case 0:
				message = "There was a problem with Last FM Api" + cause;
				break;
			case 2:
				message = "User hasnt played any song recently!" + cause;
				break;
			default:
				message = "Unknown Error";
		}
		sendMessage(e, base + message);
	}

	@Override
	public void threadableCode() {
		String[] returned;
		try {
			returned = parse(e);
			whoKnowsLogic(returned[0], Boolean.valueOf(returned[1]));
		} catch (ParseException e1) {
			switch (e1.getMessage()) {
				case "LastFM":
					errorMessage(e, 0, e1.getMessage());
					break;
				case "NoPlays":
					errorMessage(e, 2, e1.getMessage());
					break;
				case "DB":
					errorMessage(e, 1, e1.getMessage());
				default:
					errorMessage(e, 1000, e1.getMessage());
			}

		}
	}
}
