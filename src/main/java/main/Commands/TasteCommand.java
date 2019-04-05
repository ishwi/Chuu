package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ResultWrapper;
import DAO.Entities.UserInfo;
import main.ImageRenderer.imageRenderer;
import main.last.ConcurrentLastFM;
import main.last.LastFMServiceException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class TasteCommand extends MyCommandDbAccess {
	public TasteCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {
		List<String> lastfMNames;
		MessageBuilder messageBuilder = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		try {
			lastfMNames = Arrays.asList((parse(e)));
		} catch (ParseException ex) {
			sendMessage(e, "err ");
			return;
		}

		ResultWrapper resultWrapper = null;
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
				ImageIO.write(image, "jpg", b);
				byte[] img = b.toByteArray();
				if (img.length < 8388608) {
					messageBuilder.sendTo(e.getChannel()).addFile(img, "cat.png").queue();
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} catch (InstanceNotFoundException e1) {
			userNotOnDB(e);
			return;
		} catch (LastFMServiceException e1) {
			onLastFMError(e);
			return;
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
			throw new ParseException("a", 3);

		e.getChannel().sendTyping().queue();
		String[] userList = {"", ""};
		if (message.length == 1) {
			userList[1] = message[0];
			try {
				userList[0] = getDao().findShow(e.getAuthor().getIdLong()).getName();
			} catch (InstanceNotFoundException ex) {
				userNotOnDB(e);
				throw new ParseException("Base dE datos", 1);
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
			throw new ParseException("a", 3);

		}
		return new String[]{lastfMNames.get(0), lastfMNames.get(1)};
	}

	private User findUSername(String name, java.util.List<User> userList) {
		Optional<User> match = userList.stream().
				filter(user -> {
					long a = Long.valueOf(name.substring(3, name.indexOf(">")));
					return (user.getIdLong() == a);
				})
				.findFirst();
		return match.orElse(null);
	}

	private String lambda(String s, java.util.List<User> list) {
		if (s.startsWith("<@")) {
			User result = this.findUSername(s, list);
			if (result != null) {
				try {
					return getDao().findShow(result.getIdLong()).getName();
				} catch (InstanceNotFoundException e) {
					throw new RuntimeException();
				}
			}
		}
		return s;
	}

}

