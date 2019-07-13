package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ResultWrapper;
import DAO.Entities.UserInfo;
import main.Exceptions.LastFmException;
import main.ImageRenderer.TasteRenderer;
import main.Parsers.TwoUsersParser;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
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
			java.util.List<UserInfo> userInfoList = lastFM.getUserInfo(users);
			BufferedImage image = TasteRenderer.generateTasteImage(resultWrapper, userInfoList);
			sendImage(image, e);

		} catch (InstanceNotFoundException e1) {
			parser.sendError(parser.getErrorMessage(5), e);

		} catch (LastFmException e1) {
			parser.sendError(parser.getErrorMessage(2), e);


		}

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

}
