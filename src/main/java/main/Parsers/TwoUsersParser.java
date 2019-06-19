package main.Parsers;

import DAO.DaoImplementation;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class TwoUsersParser extends DaoParser {
	public TwoUsersParser(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		String[] message = getSubMessage(e.getMessage());
		if (message.length == 0) {
			sendError(getErrorMessage(0), e);
			return null;
		}

		String[] userList = {"", ""};
		if (message.length == 1) {
			userList[1] = message[0];
			try {
				userList[0] = dao.findLastFMData(e.getAuthor().getIdLong()).getName();
			} catch (InstanceNotFoundException ex) {
				sendError(getErrorMessage(5), e);
				return null;


			}
		} else {
			userList[0] = message[0];
			userList[1] = message[1];
		}

		java.util.List<String> lastfMNames;
		// Si userList contains @ -> user
		try {
			java.util.List<User> list = e.getMessage().getMentionedUsers();
			lastfMNames = Arrays.stream(userList)
					.map(s -> lambda(s, list))
					.collect(Collectors.toList());
			lastfMNames.forEach(System.out::println);
		} catch (Exception ex) {
			sendError(getErrorMessage(-1), e);
			return null;
		}
		return new String[]{lastfMNames.get(0), lastfMNames.get(1)};
	}


	private User findUsername(String name, java.util.List<User> userList) {
		Optional<User> match = userList.stream().
				filter(user -> {
					String nameNoDigits = name.replaceAll("\\D+", "");

					long a = Long.valueOf(nameNoDigits);
					return (user.getIdLong() == a);
				})
				.findFirst();
		return match.orElse(null);
	}

	private String lambda(String s, java.util.List<User> list) {
		if (s.startsWith("<@")) {
			User result = this.findUsername(s, list);
			if (result != null) {
				try {
					return dao.findLastFMData(result.getIdLong()).getName();
				} catch (InstanceNotFoundException e) {
					throw new RuntimeException(result.getName());
				}
			}
		}
		return s;
	}

	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();
		errorMessages.put(0, "Need at least one username");
		errorMessages.put(5, "User hasnt registered yet!");
		errorMessages.put(2, "Internal Server Error , try again later");
		errorMessages.put(-1, "Unknow error Happened");


	}
}
