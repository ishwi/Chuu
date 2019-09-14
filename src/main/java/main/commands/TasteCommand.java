package main.commands;

import dao.DaoImplementation;
import dao.entities.ResultWrapper;
import dao.entities.UserInfo;
import main.exceptions.LastFmException;
import main.imagerenderer.TasteRenderer;
import main.parsers.TwoUsersParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
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
	public String getDescription() {
		return "Compare Your musical taste with a  colleague";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("taste");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) {
		List<String> lastfMNames;

		String[] returned = parser.parse(e);
		if (returned == null)
			return;
		lastfMNames = Arrays.asList(returned);

		ResultWrapper resultWrapper;
		try {
			resultWrapper = getDao().getSimilarities(lastfMNames);
			//TODO this happens both when user is not on db and no mathching so fix pls
			if (resultWrapper.getRows() == 0) {
				sendMessageQueue(e, "You don't share any artist :(");
				return;
			}
			java.util.List<String> users = new ArrayList<>();
			users.add(resultWrapper.getResultList().get(0).getUserA());
			users.add(resultWrapper.getResultList().get(0).getUserB());
			java.util.List<UserInfo> userInfoList = lastFM.getUserInfo(users);
			BufferedImage image = TasteRenderer.generateTasteImage(resultWrapper, userInfoList);
			sendImage(image, e);

		} catch (LastFmException e1) {
			parser.sendError(parser.getErrorMessage(2), e);


		}

	}

	@Override
	public String getName() {
		return "Taste";
	}

}
