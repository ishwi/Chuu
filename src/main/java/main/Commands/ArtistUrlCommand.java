package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistInfo;
import main.Parsers.ArtistUrlParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class ArtistUrlCommand extends ConcurrentCommand {
	public ArtistUrlCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new ArtistUrlParser();
	}

	@Override
	public void threadablecode(MessageReceivedEvent e) {
		String urlParsed;
		String artist;

		String[] message = parser.parse(e);
		if (message == null)
			return;
		urlParsed = message[1];
		artist = message[0];
		try (InputStream in = new URL(urlParsed).openStream()) {
			BufferedImage image = ImageIO.read(in);
			if (image == null) {
				sendMessage(e, "Couldn't get an Image from link supplied");
				return;
			}

			getDao().upsertUrl(new ArtistInfo(urlParsed, artist));
			sendMessage(e, "Image of " + artist + " updated");

		} catch (IOException exception) {
			parser.sendError(parser.getErrorMessage(3), e);
			exception.printStackTrace();
		}


	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!url");
	}

	@Override
	public String getDescription() {
		return "changes artist image that is  displayed on some bot functionalities";
	}

	@Override
	public String getName() {
		return "Artist Url ";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!url artist url" +
				"\n\n");
	}


}
