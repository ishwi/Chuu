package main.commands;

import dao.DaoImplementation;
import dao.entities.LbEntry;

import java.util.Arrays;
import java.util.List;

public class AlbumCronwsLeaderboardCommand extends CrownLeaderboardCommand {
	public AlbumCronwsLeaderboardCommand(DaoImplementation dao) {
		super(dao);
		this.entryName = "Album Crowns";

	}

	@Override
	List<LbEntry> getList(long guildId) {
		return getDao().albumCrownsLeaderboard(guildId);
	}

	@Override
	public String getDescription() {
		return ("Album Crowns per user ordered desc");
	}

	@Override
	public String getName() {
		return "Album Crowns Leaderboard";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("crownsalbumlb", "crownsallb");
	}
}
