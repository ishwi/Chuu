package main.Commands;

import DAO.DaoImplementation;
import main.Parsers.WhoKnowsNpParser;

import java.util.Arrays;
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
}





