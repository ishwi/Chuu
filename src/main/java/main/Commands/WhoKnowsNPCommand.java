package main.Commands;

import DAO.DaoImplementation;
import main.Parsers.WhoKnowsNpParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WhoKnowsNPCommand extends WhoKnowsCommand {


	public WhoKnowsNPCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new WhoKnowsNpParser(getDao(), this.lastFM);

	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("!whoknowsnp", "!wknp");

	}

	@Override
	public String getDescription() {
		return "Returns list of users who know the artists you are playing right now!";
	}

	@Override
	public String getName() {
		return "Who Knows Now Playing";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!whoknowsnp *LastFmUser \n" +
				"\t If username is not specified defaults to authors account\n" +
				"\t --image for Image format\n\n");
	}


	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] returned;

		returned = parser.parse(e);
		if (returned == null)
			return;
		whoKnowsLogic(returned[0], Boolean.valueOf(returned[1]), e);
	}
}

