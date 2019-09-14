package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import DAO.Entities.LastFMData;
import DAO.Entities.UsersWrapper;
import main.Chuu;
import main.Exceptions.LastFMNoPlaysException;
import main.Parsers.OneWordParser;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SetCommand extends ConcurrentCommand {
	public SetCommand(DaoImplementation dao) {
		super(dao);
		parser = new OneWordParser();
		this.respondInPrivate = false;

	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("set");
	}

	@Override
	public String getDescription() {
		return "Adds you to the system";
	}

	@Override
	public String getName() {
		return "Set";
	}

	@Override
	public void onCommand(MessageReceivedEvent e) {
		String[] returned;

		returned = parser.parse(e);
		if (returned == null)
			return;

		MessageBuilder mes = new MessageBuilder();
		String lastFmID = returned[0];
		long guildID = e.getGuild().getIdLong();
		long userId = e.getAuthor().getIdLong();
		List<UsersWrapper> list = getDao().getAll(guildID);
		Optional<UsersWrapper> name = (list.stream().filter(user -> user.getLastFMName().equals(lastFmID)).findFirst());
		if (name.isPresent()) {
			sendMessage(e, "That username is already registered in this server sorry");
			return;
		}

		Optional<UsersWrapper> u = (list.stream().filter(user -> user.getDiscordID() == userId).findFirst());
		//User was already registered in this guild
		if (u.isPresent()) {
			//Registered with different username
			if (!u.get().getLastFMName().equals(lastFmID)) {
				sendMessage(e, "Changing your username, might take a while");
				//Remove but only from the guild if not guild removeUser all
				getDao().removeUserFromOneGuildConsequent(userId, guildID);
			} else {
				sendMessage(e, e.getAuthor().getName() + " , you are good to go!");
				return;
			}
			//First Time on the guild
		} else {
			//If it was registered in at least other  guild theres no need to update
			if (getDao().getGuildList(userId).stream().anyMatch(user -> user != guildID)) {
				//Adds the user to the guild
				getDao().addGuildUser(userId, guildID);
				sendMessage(e, e.getAuthor().getName() + " , you are good to go!");
				return;

			}
		}

		//Never registered before
		getDao().insertArtistDataList(new LastFMData(lastFmID, userId, guildID));
		mes.setContent("**" + e.getAuthor()
				.getName() + "** has set their last FM name \n Updating your library , wait a moment");
		mes.sendTo(e.getChannel()).queue();
		e.getChannel().sendTyping().queue();
		try {
			List<ArtistData> artistDataLinkedList = lastFM.getLibrary(lastFmID);
			getDao().insertArtistDataList(artistDataLinkedList, lastFmID);
			System.out.println("Updated Info Normally  of " + lastFmID + LocalDateTime
					.now().format(DateTimeFormatter.ISO_DATE));

			System.out.println(" Number of rows updated :" + artistDataLinkedList.size());
			sendMessage(e, "Finished updating " + e.getAuthor().getName() + " library, you are good to go!");
		} catch (
				LastFMNoPlaysException ex) {
			getDao().updateUserTimeStamp(lastFmID, null, null);
			sendMessage(e, "Finished updating " + e.getAuthor().getName() + "'s library, you are good to go!");

		} catch (Throwable ex) {
			System.out.println("Error while updating" + lastFmID + LocalDateTime.now()
					.format(DateTimeFormatter.ISO_DATE));
			Chuu.getLogger().warn(ex.getMessage(), ex);
			getDao().updateUserTimeStamp(lastFmID, 0, null);
			sendMessage(e, "Error  updating" + e.getAuthor()
					.getName() + "'s  library, try to use the !update command!");
		}


	}


}
