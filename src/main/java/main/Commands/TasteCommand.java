package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ResultWrapper;
import DAO.Entities.UserInfo;
import main.APIs.Parsers.TwoUsersParser;
import main.Exceptions.LastFmException;
import main.ImageRenderer.ImageRenderer;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TasteCommand extends ConcurrentCommand {
	public TasteCommand(DaoImplementation dao) {
		super(dao);
		parser = new TwoUsersParser(dao);
	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!taste");
	}

	@Override
	public String getDescription() {
		return "Compare Your musical taste with a colleage";
	}

	@Override
	public String getName() {
		return "Taste";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList
				("!taste user1 *user2\n \tIf user2 is missing it gets replaced by Author user\n\n");
	}


	@Override
	public void threadableCode(MessageReceivedEvent e) {
		List<String> lastfMNames;
		MessageBuilder messageBuilder = new MessageBuilder();


		String[] returned = parser.parse(e);
		if (returned == null)
			return;
		lastfMNames = Arrays.asList(returned);

		ResultWrapper resultWrapper;
		try {
			resultWrapper = getDao().getSimilarities(lastfMNames);
			System.out.println("resultWrapper = " + resultWrapper.getRows());
			java.util.List<String> users = new ArrayList<>();
			users.add(resultWrapper.getResultList().get(0).getUserA());
			users.add(resultWrapper.getResultList().get(0).getUserB());
			java.util.List<UserInfo> userInfoLiust = lastFM.getUserInfo(users);
			BufferedImage image = ImageRenderer.generateTasteImage(resultWrapper, userInfoLiust);

			ByteArrayOutputStream b = new ByteArrayOutputStream();
			try {
				ImageIO.write(image, "png", b);
				byte[] img = b.toByteArray();
				if (img.length < 8388608) {
					messageBuilder.sendTo(e.getChannel()).addFile(img, "taste.png").queue();
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} catch (InstanceNotFoundException e1) {
			parser.sendError(parser.getErrorMessage(5), e);

		} catch (LastFmException e1) {
			parser.sendError(parser.getErrorMessage(2), e);


		}

	}
}
