package main.Commands;

import DAO.DaoImplementation;
import main.last.ConcurrentLastFM;
import main.last.LastFMServiceException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("Duplicates")
public class ChartCommand extends MyCommandDbAccess {

	public ChartCommand(DaoImplementation dao) {
		super(dao);
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
	public void onCommand(MessageReceivedEvent e, String[] args) {
		MessageBuilder mes = new MessageBuilder();
		EmbedBuilder embed = new EmbedBuilder();
		String[] returned;
		MessageChannel cha = e.getChannel();
		try {
			returned = parse(e);
		} catch (ParseException e1) {
			new MessageBuilder("You are a dumbass").sendTo(cha).queue();
			return;
		}

		cha.sendTyping().queue();

		int x = Integer.parseInt(returned[0]);
		int y = Integer.parseInt(returned[1]);
		String username = returned[2];
		String time = returned[3];


		if (x * y > 100) {
			cha.sendMessage("Gonna Take a while").queue();
		}
		try {
			byte[] file = ConcurrentLastFM.getUserList(username, time, x, y);
			if (file.length < 8388608) {
				cha.sendFile(file, "cat.png").queue();
				return;
			}
			cha.sendMessage("boot to big").queue();


			String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
					.withZone(ZoneOffset.UTC)
					.format(Instant.now());

			String path = "D:\\Games\\" + thisMoment + ".png";
			try (FileOutputStream fos = new FileOutputStream(path)) {
				fos.write(file);
				//fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
			} catch (IOException ignored) {
			}


		} catch (LastFMServiceException ex2) {
			onLastFMError(e);
		}

	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!chart");
	}

	@Override
	public String getDescription() {
		return "Returns a Chart with albums";
	}

	@Override
	public String getName() {
		return "Chart";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("**!chart *[w,m,t,y,a] *Username SizeXSize** \n" +
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
			throw new ParseException(message[3], 0);
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
		Optional<String> thirdOptional = Arrays.stream(message).findFirst();
		if (thirdOptional.isPresent()) {
			discordName = thirdOptional.get();
		} else {
			discordName = getLastFmUsername1input(message, e.getAuthor().getIdLong(), e);
		}
		timeFrame = getTimeFromChar(timeFrame);
		return new String[]{x, y, discordName, timeFrame};
	}
}
