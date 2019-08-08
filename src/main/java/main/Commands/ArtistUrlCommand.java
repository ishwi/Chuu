package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistInfo;
import main.Chuu;
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
	public void threadableCode(MessageReceivedEvent e) {
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
				parser.sendError(parser.getErrorMessage(2), e);
				return;
			}
			String correction = CommandUtil.onlyCorrection(getDao(), artist, lastFM);
			getDao().upsertUrl(new ArtistInfo(urlParsed, correction));
			sendMessage(e, "Image of " + correction + " updated");

		} catch (IOException exception) {
			parser.sendError(parser.getErrorMessage(2), e);
			Chuu.getLogger().warn(exception.getMessage(), exception);
		}


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
	public List<String> getAliases() {
		return Collections.singletonList("!url");
	}


}
