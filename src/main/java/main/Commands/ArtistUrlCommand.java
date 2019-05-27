package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistInfo;
import main.Exceptions.ParseException;
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
	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String urlParsed;
		String artist;
		try {
			String[] message = parse(e);
			urlParsed = message[1];
			artist = message[0];
		} catch (ParseException ex) {
			switch (ex.getMessage()) {
				case "Command":
					errorMessage(e, 0, ex.getMessage());
					break;
				case "Url":
					errorMessage(e, 1, ex.getMessage());
					break;
				default:
					errorMessage(e, 100, ex.getMessage());
			}
			return;
		}
		try (InputStream in = new URL(urlParsed).openStream()) {
			BufferedImage image = ImageIO.read(in);
			if (image == null) {
				sendMessage(e, "Couldn't get an Image from link supplied");
				return;
			}

			getDao().upsertUrl(new ArtistInfo(urlParsed, artist));
			sendMessage(e, "Image of " + artist + " updated");
			return;

		} catch (IOException exception) {
			exception.printStackTrace();
			errorMessage(e, 4, "Something happened while processing the image");
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

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {

		String[] message = getSubMessage(e.getMessage());


		boolean noUrl = true;

		String artist;
		String url = null;
		if (message.length >= 2) {
			StringBuilder a = new StringBuilder();
			for (String s : message) {
				if (noUrl && CommandUtil.isValidURL(s)) {
					noUrl = false;
					url = s;
					continue;
				}
				a.append(s).append(" ");
			}
			artist = a.toString().trim();

		} else {
			throw new ParseException("Command");
		}
		if (url == null)
			throw new ParseException("Url");
		return new String[]{artist, url};

	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {

		String base = "Error processing " + e.getAuthor().getName() + "'s request:\n";
		String message;
		switch (code) {
			case 0:
				message = "Luky is a retard";
				break;
			case 1:
				message = " Invalid URL ";
				break;
			case 2:

			case 6:
				message = cause;
				break;
			case 3:
				message = cause + " is not a real lastFM username";
				break;
			case 5:
				message = "Insufficient permission to perform the command";
				break;
			default:
				message = "Unknown Error happened";
				break;
		}
		sendMessage(e, base + message);
	}

}
