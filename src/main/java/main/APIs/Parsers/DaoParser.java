package main.APIs.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.LastFMData;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.List;

public abstract class DaoParser extends Parser {
	private DaoImplementation dao;

	public DaoParser(MessageReceivedEvent e, DaoImplementation dao) {
		super(e);
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
			sendError(getErrorMessage(1));
			return null;
		}
		return username;
	}

	@Override
	public void setUpErrorMessages() {
		errorMessages.put(1, "User not on database");
	}
}
