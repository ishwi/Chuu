package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.LastFMData;
import DAO.Entities.UsersWrapper;
import main.Parsers.OneWordParser;
import main.ScheduledTasks.UpdaterThread;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
	public void threadableCode(MessageReceivedEvent e) {
		String[] returned;

		returned = parser.parse(e);
		if (returned == null)
			return;


		MessageBuilder mes = new MessageBuilder();
		String lastFmID = returned[0];
		long guildID = e.getGuild().getIdLong();
		long userId = e.getAuthor().getIdLong();
		List<UsersWrapper> list = getDao().getAll(guildID);


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
				.getName() + "** has set its last FM name \n Updating the library on the background");
		mes.sendTo(e.getChannel()).queue();

		new Thread(new UpdaterThread(getDao(), new UsersWrapper(userId, lastFmID), false)).run();
		sendMessage(e, "Finished updating " + e.getAuthor().getName() + " library, you are good to go!");
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!set");
	}

	@Override
	public String getDescription() {
		return "Adds you to the system";
	}

	@Override
	public String getName() {
		return "Set";
	}


}
