package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.LastFMData;
import DAO.Entities.UsersWrapper;
import main.Exceptions.ParseException;
import main.UpdaterThread;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class SetCommand extends MyCommandDbAccess {
	public SetCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {
		String[] returned;

		try {
			returned = parse(e);
		} catch (ParseException e1) {
			errorMessage(e, 0, e1.getMessage());
			return;
		}


		MessageBuilder mes = new MessageBuilder();
		String lastFmID = returned[0];
		long guildID = e.getGuild().getIdLong();
		long userId = e.getAuthor().getIdLong();
		List<UsersWrapper> list = getDao().getAll(guildID);
		if (list.stream().anyMatch(user -> user.getDiscordID() == userId)) {
			sendMessage(e, "Changing your username, might take a while");
			e.getChannel().sendTyping().queue();
			getDao().remove(userId);
		}

		getDao().addData(new LastFMData(lastFmID, userId, guildID));

		new Thread(new UpdaterThread(getDao(), new UsersWrapper(userId, lastFmID))).run();
		mes.setContent(e.getAuthor().getName() + "Has set his last FM name \n Updating his library on the background");
		mes.sendTo(e.getChannel()).queue();
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

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("**!set lastFMUser**\n\n");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		String[] message = getSubMessage(e.getMessage());
		if (message.length > 1 || message.length == 0)
			throw new ParseException("Command");
		return message;
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request: ";
		if (code == 0) {
			sendMessage(e, base + "You need to introduce a valid LastFm name");
		} else {
			sendMessage(e, base + "You have already set your account");
		}
	}
}
