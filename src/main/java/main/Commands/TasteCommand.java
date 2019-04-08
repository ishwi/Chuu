package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ResultWrapper;
import DAO.Entities.UserInfo;
import main.Exceptions.LastFMServiceException;
import main.Exceptions.ParseException;
import main.ImageRenderer.imageRenderer;
import main.last.ConcurrentLastFM;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TasteCommand extends ConcurrentCommand {
	public TasteCommand(DaoImplementation dao) {
		super(dao);
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
				("**!taste user1 *user2 ** \n \tIf user2 is missing it gets replaced by Author user\n\n");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		MessageBuilder mes = new MessageBuilder();

		String[] message = getSubMessage(e.getMessage());
		if (message.length == 0)
			throw new ParseException("Commands");

		e.getChannel().sendTyping().queue();
		String[] userList = {"", ""};
		if (message.length == 1) {
			userList[1] = message[0];
			try {
				userList[0] = getDao().findShow(e.getAuthor().getIdLong()).getName();
			} catch (InstanceNotFoundException ex) {
				throw new ParseException("bd");
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
			throw new ParseException(ex.getMessage());

		}
		return new String[]{lastfMNames.get(0), lastfMNames.get(1)};
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request: ";
		String message;
		switch (code) {
			case 0:
				message = "Need at least one argument!";
				break;
			case 1:
				message = "User not on db ,register first!";
				break;
			case 2:
				message = "User " + cause + " hasnt registered yet!";
				break;
			case 3:
				message = "There was a problem with Last FM Api" + cause;
				break;
			default:
				message = "Unknown Error happened";
				break;
		}
		sendMessage(e, base + message);
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
					return getDao().findShow(result.getIdLong()).getName();
				} catch (InstanceNotFoundException e) {
					throw new RuntimeException(result.getName());
				}
			}
		}
		return s;
	}

	@Override
	public void threadableCode() {
		List<String> lastfMNames;
		MessageBuilder messageBuilder = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		try {
			lastfMNames = Arrays.asList((parse(e)));
		} catch (ParseException ex) {
			switch (ex.getMessage()) {
				case "Commands":
					errorMessage(e, 0, ex.getMessage());
					break;
				case "db":
					errorMessage(e, 1, ex.getMessage());
					break;
				default:
					errorMessage(e, 2, ex.getMessage());
					break;
			}
			return;
		}
		ResultWrapper resultWrapper;
		try {
			resultWrapper = getDao().getSimilarities(lastfMNames);
			System.out.println("resultWrapper = " + resultWrapper.getRows());
			java.util.List<String> users = new ArrayList<>();
			users.add(resultWrapper.getResultList().get(0).getUserA());
			users.add(resultWrapper.getResultList().get(0).getUserB());
			java.util.List<UserInfo> userInfoLiust = ConcurrentLastFM.getUserInfo(users);
			BufferedImage image = imageRenderer.generateTasteImage(resultWrapper, userInfoLiust);

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
			errorMessage(e, 1, e1.getMessage());
		} catch (LastFMServiceException e1) {
			errorMessage(e, 3, e1.getMessage());
		}


	}
}

