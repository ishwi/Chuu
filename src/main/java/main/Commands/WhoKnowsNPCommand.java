package main.Commands;

import DAO.DaoImplementation;
import main.Parsers.WhoKnowsNpParser;

import java.util.Arrays;
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

}

