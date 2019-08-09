package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ResultWrapper;
import DAO.Entities.UserInfo;
import main.Exceptions.LastFmException;
import main.ImageRenderer.TasteRenderer;
import main.Parsers.TwoUsersParser;
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
	public void threadableCode(MessageReceivedEvent e) {
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
				sendMessage(e, "You don't share any artist :(");
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
	public String getDescription() {
		return "Compare Your musical taste with a  colleague";
	}

	@Override
	public String getName() {
		return "Taste";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!taste");
	}

}
