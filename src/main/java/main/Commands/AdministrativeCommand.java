package main.Commands;

import DAO.DaoImplementation;
import main.Exceptions.ParseException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.map.MultiValueMap;
import org.imgscalr.Scalr;

import javax.annotation.Nonnull;
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
	public void threadableCode(MessageReceivedEvent e) {
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
				case "Permissions":
					errorMessage(e, 5, ex.getMessage());
					break;
				default:
					errorMessage(e, 100, ex.getMessage());
			}
			return;
		}
		try (InputStream in = new URL(urlParsed).openStream()) {
			BufferedImage image = ImageIO.read(in);
			image = Scalr.resize(image, Scalr.Method.QUALITY, 75, Scalr.OP_ANTIALIAS);
			if (image == null) {
				errorMessage(e, 6, "Could get an Image from link supplied");
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
	public void onGuildMemberLeave(@Nonnull GuildMemberLeaveEvent event) {

		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");
		System.out.println("USER LEAVED");

		Executors.newSingleThreadExecutor()
				.execute(() ->
						getDao().remove(event.getMember().getIdLong())
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
		return Collections.singletonList("!logo url" +
				"\n\t User need to have administration permisions\n\n");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		if (!e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			throw new ParseException("Permissions");
		}
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

			case 6:
				message = cause;
				break;
			case 3:
				message = cause + " is not a real lastFM username";
				break;
			case 5:
				message = "Insufficient permission to perform the command";
				break;
			default:
				message = "Unknown Error happened";
				break;
		}
		sendMessage(e, base + message);
	}


}
