package main.Commands;

import DAO.DaoImplementation;
import main.Parsers.WhoKnowsNpParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class BandInfoNpCommand extends BandInfoCommand {
	public BandInfoNpCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new WhoKnowsNpParser(getDao(), this.lastFM);
	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] returned;

		returned = parser.parse(e);
		if (returned == null)
			return;
		whoKnowsLogic(returned[0], Boolean.valueOf(returned[1]), e);
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
}





