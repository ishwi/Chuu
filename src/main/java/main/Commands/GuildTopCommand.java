package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.UrlCapsule;
import main.ImageRenderer.GuildMaker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class GuildTopCommand extends ConcurrentCommand {

	public GuildTopCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {


		List<UrlCapsule> resultWrapper = getDao().getGuildTop(e.getGuild().getIdLong());
		BufferedImage image = GuildMaker.generateCollageThreaded(5, 5, new LinkedBlockingDeque<>(resultWrapper));
		ByteArrayOutputStream b = new ByteArrayOutputStream();

		try {
			ImageIO.write(image, "png", b);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		byte[] img = b.toByteArray();
		if (img.length < 8388608) {
			e.getChannel().sendFile(img, "cat.png").queue();
			return;
		}
		e.getChannel().sendMessage("boot to big").queue();


		String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());

		String path = "D:\\Games\\" + thisMoment + ".png";
		try (FileOutputStream fos = new FileOutputStream(path)) {
			fos.write(img);
			//fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
		} catch (IOException ex) {
			sendMessage(e, "Ish pc bad");
		}

	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!guild");
	}

	@Override
	public String getDescription() {
		return ("Chart 5x5 of guild most listened artist");
	}

	@Override
	public String getName() {
		return "Guild Top Artists";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!guild \n");
	}


}



