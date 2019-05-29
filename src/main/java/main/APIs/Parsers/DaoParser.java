package main.APIs.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.LastFMData;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.List;

public abstract class DaoParser extends Parser {
	public DaoImplementation dao;

	DaoParser(DaoImplementation dao) {
		super();
		this.dao = dao;
	}

	String getLastFmUsername1input(String[] message, Long id, MessageReceivedEvent event) {
		String username;
		try {
			if (message.length != 1) {
				username = this.dao.findShow(id).getName();
			} else {
				//Caso con @ y sin @
				List<User> list = event.getMessage().getMentionedUsers();
				username = message[0];
				if (!list.isEmpty()) {
					LastFMData data = this.dao.findShow((list.get(0).getIdLong()));
					username = data.getName();
				}
				if (username.startsWith("@")) {
					event.getChannel().sendMessage("Trolled xD").queue();
					return null;
				}
			}
		} catch (InstanceNotFoundException e) {
			sendError(getErrorMessage(1), event);
			return null;
		}
		return username;
	}

	@Override
	public void setUpErrorMessages() {
		errorMessages.put(1, "User not on database");
		errorMessages.put(3, "User hasnt played any songs recently");
		errorMessages.put(4, "User does not exist on last.fm");
	}
}
