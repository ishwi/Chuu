package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.UrlCapsule;
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

@SuppressWarnings("Duplicates")
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


		if (x * y > 100) {
			e.getChannel().sendMessage("Gonna Take a while").queue();
		}
		try {
			processQueue(username, time, x, y, e);


		} catch (LastFmEntityNotFoundException e1) {
			parser.sendError(parser.getErrorMessage(3), e);
		} catch (LastFmException ex2) {
			parser.sendError(parser.getErrorMessage(4), e);
		}


	}

	public void processQueue(String username, String time, int x, int y, MessageReceivedEvent e) throws LastFmException {
		BlockingQueue<UrlCapsule> queue = new LinkedBlockingDeque<>();
		lastFM.getUserList(username, time, x, y, true, queue);
		generateImage(queue, x, y, e);
	}

	void generateImage(BlockingQueue<UrlCapsule> queue, int x, int y, MessageReceivedEvent e) {
		int size = queue.size();
		int minx = (int) Math.ceil((double) size / x);
		//int miny = (int) Math.ceil((double) size / y);
		if (minx == 1)
			x = size;
		BufferedImage image = CollageMaker.generateCollageThreaded(x, minx, queue);
		sendImage(image, e);
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


}
