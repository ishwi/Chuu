package main.Commands;

import DAO.DaoImplementation;
import main.Exceptions.ParseException;
import net.dv8tion.jda.client.events.group.GroupUserLeaveEvent;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.map.MultiValueMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AdministrativeCommand extends ConcurrentCommand {

	public AdministrativeCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (!e.getMember().hasPermission(Permission.ADMINISTRATOR) || !e.getMember().isOwner())
			return;
		super.onMessageReceived(e);
	}

	@Override
	public void threadableCode() {
		String urlParsed;
		try {
			urlParsed = parse(e)[0];
		} catch (ParseException ex) {
			switch (ex.getMessage()) {
				case "Command":
					errorMessage(e, 0, ex.getMessage());
					break;
				case "Url":
					errorMessage(e, 1, ex.getMessage());
					break;
			}
			return;
		}
		try (InputStream in = new URL(urlParsed).openStream()) {
			BufferedImage image = ImageIO.read(in);
			if (image.getWidth() > 150 || image.getHeight() > 150) {
				errorMessage(e, 3, "File should be smaller than 150x150!");
				return;
			}
			getDao().addLogo(e.getGuild().getIdLong(), image);
			sendMessage(e, "Logo Updated");
			return;
		} catch (IOException exception) {
			exception.printStackTrace();
			errorMessage(e, 4, "Something happened while processing the image");
		}


	}

	@Override
	public void onGroupUserLeave(GroupUserLeaveEvent event) {
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");

		Executors.newSingleThreadExecutor()
				.execute(() ->
						getDao().remove(event.getUser().getIdLong())
				);
	}


	public void onStartup(JDA jda) {
		MultiValueMap<Long, Long> map = getDao().getMapGuildUsers();
		//
		List<Long> usersIMightLikeToDelete = new ArrayList<>();
		List<Long> usersNotDeleted = new ArrayList<>();

		map.forEach((key, value) -> {
			List<Long> usersToDelete;
			List<Long> user = (List<Long>) map.getCollection(key);
			Guild guild = jda.getGuildById(key);
			if (guild != null) {
				List<Member> memberList = guild.getMembers();
				List<Long> guildList = memberList.stream().map(x -> x.getUser().getIdLong()).collect(Collectors.toList());
				usersToDelete = user.stream().filter(eachUser -> !guildList.contains(eachUser)).collect(Collectors.toList());
				usersNotDeleted.addAll(user.stream().filter(guildList::contains).collect(Collectors.toList()));

				//usersToDelete.forEach(dao::removeUser);
				usersIMightLikeToDelete.addAll(usersToDelete);
			} else {
				//When the bot is not presente on a guild check what users to delete;
				usersIMightLikeToDelete.addAll(user);

			}
		});
		for (Long potentiallyDeletedUser : usersIMightLikeToDelete) {
			if (!usersNotDeleted.contains(potentiallyDeletedUser))
				getDao().remove(potentiallyDeletedUser);
		}


	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!logo");
	}

	@Override
	public String getDescription() {
		return "Adds logo to be displayed on some bot functionalities";
	}

	@Override
	public String getName() {
		return "Logo";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!logo [*url|embedded file?]" +
				"\n\t User need to have administration permisions\n\n");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		String[] subMessage = getSubMessage(e.getMessage());
		if (subMessage.length != 1)
			throw new ParseException("Command");
		try {
			URL url = (new URL(subMessage[0]));
			return new String[]{url.toString()};
		} catch (IOException ex) {
			throw new ParseException("Url");
		}
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {

		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request:\n";
		String message;
		switch (code) {
			case 0:
				message = "You introduced too many words";
				break;
			case 1:
				message = " Invalid URL ";
				break;
			case 2:
			case 4:
				message = cause;
				break;
			case 3:
				message = cause + " is not a real lastFM username";
				break;
			default:
				message = "Unknown Error happened";
				break;
		}
		sendMessage(e, base + message);
	}


}
