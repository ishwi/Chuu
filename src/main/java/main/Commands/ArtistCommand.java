package main.Commands;

import DAO.DaoImplementation;
import main.APIs.Discogs.DiscogsApi;
import main.APIs.Discogs.DiscogsSingleton;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Exceptions.ParseException;
import main.ImageRenderer.UrlCapsuleConcurrentQueue;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("Duplicates")
public class ArtistCommand extends ChartCommand {
	private final DiscogsApi discogsApi;

	public ArtistCommand(DaoImplementation dao) {
		super(dao);
		discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] returned;
		try {
			returned = parse(e);
		} catch (ParseException e1) {
			switch (e1.getMessage()) {
				case "DB":
					errorMessage(e, 0, e1.getMessage());
					break;
				case "Command":
					errorMessage(e, 1, e1.getMessage());
					break;
				default:
					errorMessage(e, 1000, e1.getMessage());
			}
			return;
		}


		int x = Integer.parseInt(returned[0]);
		int y = Integer.parseInt(returned[1]);
		String username = returned[2];
		String time = returned[3];


		if (x * y > 100) {
			e.getChannel().sendMessage("Gonna Take a while").queue();
		}
		try {

			UrlCapsuleConcurrentQueue queue = new UrlCapsuleConcurrentQueue(getDao(), discogsApi);
			lastFM.getUserList(username, time, x, y, false, queue);
			generateImage(queue, x, y, e);


		} catch (LastFmEntityNotFoundException e1) {
			errorMessage(e, 3, e1.getMessage());
		} catch (LastFmException ex) {
			errorMessage(e, 2, ex.getMessage());

		}


	}

	private String getTimeFromChar(String timeFrame) {
		if (timeFrame.startsWith("y"))
			return "12month";
		if (timeFrame.startsWith("t"))
			return "3month";
		if (timeFrame.startsWith("m"))
			return "1month";
		if (timeFrame.startsWith("a"))
			return "overall";
		return "7day";
	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!artist");
	}

	@Override
	public String getDescription() {
		return "Returns a Chart with artist";
	}

	@Override
	public String getName() {
		return "Artists";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!artists *[w,m,t,y,a] *Username *SizeXSize \n" +
				"\tIf timeframe is not specified defaults to Weekly \n" +
				"\tIf username is not specified defaults to authors account \n" +
				"\tIf size is not specified defaults to 5x5 (As big as discord lets\n\n"
		);
	}


	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
//
//        1     !command

//        2     timeFrame 1 char
//        3     Username whatever
//        4     Size    somethingXsomething
		String timeFrame = "w";
		String discordName;
		String x = "5";
		String y = "5";

		String pattern = "\\d+[xX]\\d+";
		String[] message = getSubMessage(e.getMessage());

		if (message.length > 3) {
			throw new ParseException("Command");
		}

		Stream<String> firstStream = Arrays.stream(message).filter(s -> s.matches(pattern));
		Optional<String> opt = firstStream.filter(s -> s.matches(pattern)).findAny();
		if (opt.isPresent()) {
			x = (opt.get().split("[xX]")[0]);
			y = opt.get().split("[xX]")[1];
			message = Arrays.stream(message).filter(s -> !s.equals(opt.get())).toArray(String[]::new);

		}

		Stream<String> secondStream = Arrays.stream(message).filter(s -> s.length() == 1 && s.matches("[ytmwao]"));
		Optional<String> opt2 = secondStream.findAny();
		if (opt2.isPresent()) {
			timeFrame = opt2.get();
			message = Arrays.stream(message).filter(s -> !s.equals(opt2.get())).toArray(String[]::new);

		}

		discordName = getLastFmUsername1input(message, e.getAuthor().getIdLong(), e);

		timeFrame = getTimeFromChar(timeFrame);
		return new String[]{x, y, discordName, timeFrame};
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {

		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request:\n";
		String message;
		switch (code) {
			case 1:
				message = "You introduced too many words";
				break;
			case 0:
				userNotOnDB(e, 0);
				return;
			case 2:
				message = "Internal Server Error, Try again later";
				break;
			case 3:
				message = cause + " is not a real lastFM username";
				break;
			default:
				message = "Unknown Error happened";
				break;
		}
		sendMessage(e, base + message);
	}


}
