package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.LbEntry;

import java.util.Collections;
import java.util.List;

public class ArtistCountLeaderboard extends CrownLeaderboardCommand {
	public ArtistCountLeaderboard(DaoImplementation dao) {
		super(dao);
		this.entryName = "artist";
	}

	@Override
	public List<LbEntry> getList(long guildId) {
		return getDao().getArtistLeaderboard(guildId);
	}


	@Override
	public String getDescription() {
		return ("Artists count per user ordered desc");
	}

	@Override
	public String getName() {
		return "Artist count Leaderboard";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!scrobbledlb");
	}

}
