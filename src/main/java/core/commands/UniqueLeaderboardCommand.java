package core.commands;

import dao.DaoImplementation;
import dao.entities.LbEntry;

import java.util.Collections;
import java.util.List;

public class UniqueLeaderboardCommand extends CrownLeaderboardCommand {
	public UniqueLeaderboardCommand(DaoImplementation dao) {
		super(dao);
		this.entryName = "Unique Artists";
	}

	@Override
	public String getDescription() {
		return ("Unique Artist leaderboard in guild ");
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("uniquelb");
	}

	@Override
	public List<LbEntry> getList(long guildId) {
		return getDao().getUniqueLeaderboard(guildId);
	}

	@Override
	public String getName() {
		return "Unique Leaderboard";
	}

}
