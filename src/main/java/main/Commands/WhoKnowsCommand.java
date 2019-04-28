package main.Commands;

import DAO.DaoImplementation;
import main.Exceptions.ParseException;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
public class WhoKnowsCommand extends ConcurrentCommand {
	public WhoKnowsCommand(DaoImplementation dao) {
		super(dao);
	}


	@Override
	public void threadableCode() {
		String[] returned;
		try {
			returned = parse(e);
			CommandUtil.a(returned[0], getDao(), e, Boolean.valueOf(returned[1]));
		} catch (ParseException e1) {
			errorMessage(e, 0, e1.getMessage());
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


		boolean flag = false;
		String[] message1 = Arrays.stream(message).filter(s -> !s.equals("--image")).toArray(String[]::new);
		if (message1.length != message.length) {
			message = message1;
			flag = true;
		}
		if (message.length == 0) {
			//No Commands Inputed
			throw new ParseException("Input");
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

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request: ";
		if (code == 0) {
			sendMessage(e, base + " You need to specify the Artist!");
			return;
		}
		sendMessage(e, base + "Unknown Error");
	}
}
