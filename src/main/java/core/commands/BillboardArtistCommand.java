package core.commands;

import dao.ChuuService;

import java.util.List;

public class BillboardArtistCommand extends BillboardCommand {
    public BillboardArtistCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public String getDescription() {
        return "The most popular artists last week on this server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("artistbillboard", "trendartist", "atrend", "trenda");
    }

    @Override
    public String getName() {
        return "Server's Artists Billboard Top 100";
    }

    @Override
    public String getTitle() {
        return "Artists";
    }

    @Override
    public List<BillboardEntity> getEntities(int weekId, long guildId, boolean doListeners) {
        return getService().getArtistBillboard(weekId, guildId, doListeners);
    }
}
