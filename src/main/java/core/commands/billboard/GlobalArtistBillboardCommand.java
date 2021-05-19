package core.commands.billboard;

import core.commands.Context;
import dao.ServiceView;
import dao.entities.BillboardEntity;

import java.util.List;

public class GlobalArtistBillboardCommand extends GlobalBillboardCommand {
    public GlobalArtistBillboardCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    public String getDescription() {
        return "The most popular artists last week on the bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalartistbillboard", "globaltrendartist", "gatrend", "gtrenda");
    }

    @Override
    public String getName() {
        return "Bot's Artists Billboard Top 100";
    }

    @Override
    public String getTitle() {
        return "Artists ";
    }

    @Override
    public List<BillboardEntity> getEntities(int weekId, boolean doListeners, Context e) {
        return db.getGlobalArtistBillboard(weekId, doListeners);
    }
}
