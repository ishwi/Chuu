package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.LastFMData;
import main.last.UpdaterThread;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.ParseException;
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
			sendMessage(e, "");
			return;
		}
		MessageBuilder mes = new MessageBuilder();
		String lastFmID = returned[0];
		long guildID = e.getGuild().getIdLong();
		getDao().addData(new LastFMData(lastFmID, e.getAuthor().getIdLong(), guildID));

		new Thread(new UpdaterThread(getDao())).run();
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
			throw new ParseException("am", 1);
		return message;
	}
}
