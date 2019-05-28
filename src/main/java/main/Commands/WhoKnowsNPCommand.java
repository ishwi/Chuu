package main.Commands;

import DAO.DaoImplementation;
import main.APIs.Discogs.DiscogsApi;
import main.APIs.Parsers.Parser;
import main.APIs.Parsers.WhoKnowsNpParser;
import main.Exceptions.ParseException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class WhoKnowsNPCommand extends WhoKnowsCommand {


	public WhoKnowsNPCommand(DaoImplementation dao, DiscogsApi discogsApi) {
		super(dao, discogsApi);
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!whoknowsnp");
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
				"\t If useranme is not specified defaults to authors account\n" +
				"\t --image for Image format\n\n");
	}


	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		return null;
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {

	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] returned;
		Parser parser = new WhoKnowsNpParser(e, getDao(), this.lastFM);

		returned = parser.parse();
		if (returned == null)
			return;
		whoKnowsLogic(returned[0], Boolean.valueOf(returned[1]), e);
	}
}

