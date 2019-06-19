package main.Commands;

import DAO.DaoImplementation;
import main.Parsers.UrlParser;
import net.dv8tion.jda.api.JDA;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AdministrativeCommand extends ConcurrentCommand {

	public AdministrativeCommand(DaoImplementation dao) {
		super(dao);
		parser = new UrlParser();
	}


	@Override
	public void threadablecode(MessageReceivedEvent e) {
		String urlParsed;

		urlParsed = parser.parse(e)[0];

		try (InputStream in = new URL(urlParsed).openStream()) {
			BufferedImage image = ImageIO.read(in);
			image = Scalr.resize(image, Scalr.Method.QUALITY, 75, Scalr.OP_ANTIALIAS);
			if (image == null) {
				sendMessage(e, "Couldn't get an Image from link supplied");
				return;
			}

			getDao().addLogo(e.getGuild().getIdLong(), image);
			sendMessage(e, "Logo Updated");
		} catch (IOException exception) {
			exception.printStackTrace();
			sendMessage(e, "Something Happened while processing the image ");
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
				.execute(() -> {
							getDao().removeFromGuild(event.getMember().getIdLong(), event.getGuild().getIdLong());
							MultiValueMap<Long, Long> map = getDao().getMapGuildUsers();
							Collection<Long> list = map.getCollection(event.getMember().getIdLong());
							if (list == null || list.isEmpty()) {
								getDao().remove(event.getMember().getIdLong());
							}
						}
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


}
