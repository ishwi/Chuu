package main.Commands;

import DAO.DaoImplementation;
import main.Parsers.WhoKnowsNpParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BandInfoNpCommand extends BandInfoCommand {
	public BandInfoNpCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new WhoKnowsNpParser(getDao(), this.lastFM);
	}


	@Override
	public List<String> getAliases() {
		return Arrays.asList("!bandnp", "!bnp");

	}

	@Override
	public String getDescription() {
		return "Like band but for now playing artist!";
	}

	@Override
	public String getName() {
		return "Band image ";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!band *LastFmUser \n" +
				"\t If useranme is not specified defaults to authors account\n\n");
	}


	@Override
	public void threadablecode(MessageReceivedEvent e) {
		String[] returned;

		returned = parser.parse(e);
		if (returned == null)
			return;
		whoKnowsLogic(returned[0], Boolean.valueOf(returned[1]), e);
	}

}
