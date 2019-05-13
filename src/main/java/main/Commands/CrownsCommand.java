package main.Commands;

import DAO.DaoImplementation;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import main.Exceptions.ParseException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class CrownsCommand extends ConcurrentCommand {
	private EventWaiter wait;

	public CrownsCommand(DaoImplementation dao, EventWaiter wait) {
		super(dao);
		this.wait = wait;

	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
//		String[] message;
//		try {
//			message = parse(e);
//		} catch (ParseException e1) {
//			errorMessage(e, 0, e1.getMessage());
//			return;
//		}
//		Paginator.Builder pbuilder = new Paginator.Builder().setColumns(1)
//				.setItemsPerPage(10)
//				.showPageNumbers(true)
//				.setUsers(e.getGuild().getMembers().stream().map(Member::getUser).toArray(User[]::new))
//				.waitOnSinglePage(false)
//				.useNumberedItems(true)
//				.setRoles(e.getGuild().getPublicRole())
//				.setFinalAction(m -> {
//					try {
//						m.clearReactions().queue();
//					} catch (PermissionException ex) {
//						ex.printStackTrace();
//					}
//				})
//				.setEventWaiter(this.wait)
//				.setTimeout(2, TimeUnit.MINUTES);
//
//		pbuilder.clearItems();
//		List<UniqueData> resultWrapper = getDao().getCrowns(message[0], e.getGuild().getIdLong());
//
//		resultWrapper.stream().map(g -> "**[" + g.getArtistName() + "](https://www.last.fm/music/" + g.getArtistName().replaceAll(" ", "+") +
//				")** - " + g.getCount() + " plays")
//				.forEach(pbuilder::addItems);
//		Paginator p = pbuilder.setColor(CommandUtil.randomColor())
//				.setText("**" + message[0] + " ** has " + resultWrapper.size() + " crowns")
//				.setUsers(e.getAuthor())
//				.build();
//		p.display(e.getChannel());
//		return;
	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!crowns");
	}

	@Override
	public String getDescription() {
		return ("List of artist you are the top listener from");
	}

	@Override
	public String getName() {
		return "Your own top";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("**!crowns **user \n" +
				"If user is missing defaults to user account\n\n ");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {

		String[] message = getSubMessage(e.getMessage());
		return new String[]{getLastFmUsername1input(message, e.getAuthor().getIdLong(), e)};
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


}



