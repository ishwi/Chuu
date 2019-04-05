package main.Commands;

import DAO.DaoImplementation;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
public class WhoKnowsCommand extends MyCommandDbAccess {
	public WhoKnowsCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {

		String[] returned;
		try {
			returned = parse(e);
			CommandUtil.a(returned[0], getDao(), e, Boolean.valueOf(returned[1]));
		} catch (ParseException e1) {
			new MessageBuilder("You are a dumbass").sendTo(e.getChannel()).queue();
			return;
		}

	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!whoknows");
	}

	@Override
	public String getDescription() {
		return "Returns List Of Users Who Know the inputed Artist";
	}

	@Override
	public String getName() {
		return "Who Knows";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("**!whoknows artist** \n" +
				"\t --image for Image format\n\n"
		);
	}


	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {

		String[] message = getSubMessage(e.getMessage());

		if (message.length == 0) {
			//No Commands Inputed
			throw new ParseException("", 0);
		}
		boolean flag = false;
		String[] message1 = Arrays.stream(message).filter(s -> !s.equals("--image")).toArray(String[]::new);
		if (message1.length != message.length) {
			message = message1;
			flag = true;
		}
		String artist;
		if (message.length > 1) {
			StringBuilder a = new StringBuilder();
			for (String s : message) {
				a.append(s).append(" ");
			}
			artist = a.toString().trim();
		} else {
			artist = message[0];
		}
		return new String[]{artist, Boolean.toString(flag)};

	}
}
