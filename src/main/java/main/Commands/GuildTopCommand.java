package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.UniqueData;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import main.Exceptions.ParseException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;

import javax.annotation.Nullable;
import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GuildTopCommand extends ConcurrentCommand {
	EventWaiter wait;

	public GuildTopCommand(DaoImplementation dao, EventWaiter wait) {
		super(dao);
		this.wait = wait;

	}

	@Override
	public void threadableCode() {

		Paginator.Builder pbuilder = new Paginator.Builder().setColumns(1)
				.setItemsPerPage(10)
				.showPageNumbers(true)
				.waitOnSinglePage(false)
				.useNumberedItems(true)
				.setFinalAction(m -> {
					try {
						m.clearReactions().queue();
					} catch (PermissionException ex) {
						m.delete().queue();
					}
				})
				.setEventWaiter(this.wait)
				.setTimeout(2, TimeUnit.MINUTES);

		pbuilder.clearItems();
		List<UniqueData> resultWrapper = getDao().getGuildTop(e.getGuild().getIdLong());

		resultWrapper.stream().map(g -> "**[" + g.getArtistName() + "](https://www.last.fm/music/" + g.getArtistName().replaceAll(" ", "+") +
				")** - " + g.getCount() + " plays")
				.forEach(pbuilder::addItems);
		Paginator p = pbuilder.setColor(CommandUtil.randomColor())
				.setText("**" + e.getGuild().getName() + "'s** top Artists")
				.setUsers(e.getAuthor())
				.build();
		p.display(e.getChannel());
		return;
	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!guild");
	}

	@Override
	public String getDescription() {
		return ("Returns lists of all the unique artits you have scrobbled");
	}

	@Override
	public String getName() {
		return "Unique List Of Artists";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("**!unique **user \n" +
				"If user is missing defaults to user account\n\n ");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {

		String[] subMessage = getSubMessage(e.getMessage());
		if (subMessage.length == 0) {
			try {
				return new String[]{getDao().findShow(e.getAuthor().getIdLong()).getName()};
			} catch (InstanceNotFoundException e1) {
				throw new ParseException("DB");

			}
		}
		if (subMessage.length != 1) {
			throw new ParseException("Commands");
		}
		return new String[]{getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e)
		};
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request:\n";
		String message;
		switch (code) {
			case 0:
				message = "You need to introduce an user";
				break;
			case 1:
				message = "User was not found on the database, register first!";
				break;
			default:
				message = "An unknown Error happened";

		}
		sendMessage(e, base + message);
	}

	protected boolean isValidUser(User user, @Nullable Guild guild) {
		if (user.isBot())
			return false;
		if (guild == null || !guild.isMember(user))
			return false;

		return true;
	}
}



