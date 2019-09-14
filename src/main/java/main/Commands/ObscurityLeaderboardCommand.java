package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.LbEntry;

import java.util.Arrays;
import java.util.List;

public class ObscurityLeaderboardCommand extends CrownLeaderboardCommand {
	public ObscurityLeaderboardCommand(DaoImplementation dao) {
		super(dao);
		this.entryName = "Obscurity points";

	}

	@Override
	List<LbEntry> getList(long guildId) {
		return getDao().getObscurityRankings(guildId);
	}

	@Override
	public String getDescription() {
		return "Gets how obscure your scrobbled artist are in relation with all the rest of the users of the bot (Not only from this server)";
	}

	@Override
	public String getName() {
		return "Obscurity";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("obscuritylb", "ob", "obs");
	}
}
