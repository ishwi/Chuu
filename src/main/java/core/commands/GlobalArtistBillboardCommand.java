package core.commands;

import dao.ChuuService;

import java.util.List;

public class GlobalArtistBillboardCommand extends GlobalBillboardCommand {
    public GlobalArtistBillboardCommand(ChuuService dao) {
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
        return "Artists";
    }

    @Override
    public List<BillboardEntity> getEntities(int weekId, boolean doListeners) {
        return getService().getGlobalAlbumBillboard(weekId, doListeners);
    }
}
