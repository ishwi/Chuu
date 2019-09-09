package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.LastFMData;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.List;

public abstract class DaoParser extends Parser {
	final DaoImplementation dao;

	DaoParser(DaoImplementation dao) {
		super();
		this.dao = dao;
	}

	String getLastFmUsername1input(String[] message, Long id, MessageReceivedEvent event) {
		try {
			if (event.isFromGuild()) {
				LastFMData data;
				List<User> list = event.getMessage().getMentionedUsers();
				data = list.isEmpty()
						? this.dao.findLastFMData((id))
						: this.dao.findLastFMData(list.get(0).getIdLong());

				return data.getName();
			} else {
				return this.dao.findLastFMData((id)).getName();
			}
		} catch (InstanceNotFoundException e) {
			sendError(getErrorMessage(1), event);
			return null;
		}
	}
	@Override
	protected void setUpErrorMessages() {
		errorMessages.put(1, "User not on database");
		errorMessages.put(2, "Internal Server Error, try again later");
		errorMessages.put(3, "User hasn't played anything recently");
		errorMessages.put(4, "User does not exist on last.fm");
	}
}
