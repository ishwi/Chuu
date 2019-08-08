package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.UrlCapsule;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.ImageRenderer.CollageMaker;
import main.Parsers.ChartParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ChartCommand extends ConcurrentCommand {

	public ChartCommand(DaoImplementation dao) {

		super(dao);
		this.parser = new ChartParser(getDao());
	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;

		int x = Integer.parseInt(returned[0]);
		int y = Integer.parseInt(returned[1]);
		String username = returned[2];
		String time = returned[3];
		boolean titleWrite = !Boolean.parseBoolean(returned[5]);
		boolean playsWrite = Boolean.parseBoolean(returned[6]);

		if (x * y > 100) {
			e.getChannel().sendMessage("Gonna Take a while").queue();
		}
		try {
			processQueue(username, time, x, y, e, titleWrite, playsWrite);

		} catch (LastFMNoPlaysException e1) {
			parser.sendError(parser.getErrorMessage(3), e);
		} catch (LastFmEntityNotFoundException e1) {
			parser.sendError(parser.getErrorMessage(4), e);
		} catch (LastFmException ex2) {
			parser.sendError(parser.getErrorMessage(2), e);
		}


	}

	void processQueue(String username, String time, int x, int y, MessageReceivedEvent e, boolean writeTitles, boolean writePlays) throws LastFmException {
		BlockingQueue<UrlCapsule> queue = new LinkedBlockingDeque<>();
		lastFM.getUserList(username, time, x, y, true, queue);
		generateImage(queue, x, y, e, writeTitles, writePlays);
	}

	void generateImage(BlockingQueue<UrlCapsule> queue, int x, int y, MessageReceivedEvent e, boolean writeTitles, boolean writePlays) {
		int size = queue.size();
		int minx = (int) Math.ceil((double) size / x);
		//int miny = (int) Math.ceil((double) size / y);
		if (minx == 1)
			x = size;
		boolean makeSmaller = false;
		if (x * y > 42)
			makeSmaller = true;
		BufferedImage image = CollageMaker
				.generateCollageThreaded(x, minx, queue, writeTitles, writePlays, makeSmaller);
		sendImage(image, e, makeSmaller);
//
//		String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
//				.withZone(ZoneOffset.UTC)
//				.format(Instant.now());
//
//		String path = "D:\\Games\\" + thisMoment + ".png";
//		try (FileOutputStream fos = new FileOutputStream(path)) {
//			fos.write(img);
//			//fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
//		} catch (IOException ex) {
//			sendMessage(e, "Ish pc is bad ");
//		}

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
	public List<String> getAliases() {
		return Collections.singletonList("!chart");
	}


}
