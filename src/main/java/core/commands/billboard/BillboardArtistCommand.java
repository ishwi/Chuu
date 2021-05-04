package core.commands.billboard;

import core.commands.Context;
import dao.ChuuService;
import dao.entities.BillboardEntity;

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
        return "Artists ";
    }

    @Override
    public List<BillboardEntity> getEntities(int weekId, long guildId, boolean doListeners, Context event) {
        return db.getArtistBillboard(weekId, guildId, doListeners);
    }
}
