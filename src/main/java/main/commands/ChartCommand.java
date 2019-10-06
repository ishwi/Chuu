package main.commands;

import dao.DaoImplementation;
import dao.entities.UrlCapsule;
import main.exceptions.LastFmException;
import main.imagerenderer.CollageMaker;
import main.parsers.ChartParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
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
	public String getDescription() {
		return "Returns a Chart with albums";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("chart");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
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
			e.getChannel().sendMessage("Going to take a while").queue();
		}

		processQueue(username, time, x, y, e, titleWrite, playsWrite);


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
		if (size > 45)
			makeSmaller = true;
		BufferedImage image = CollageMaker
				.generateCollageThreaded(x, minx, queue, writeTitles, writePlays, makeSmaller);
		sendImage(image, e, makeSmaller);
	}

	@Override
	public String getName() {
		return "Chart";
	}


}
