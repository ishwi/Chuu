package core.commands;

import dao.ChuuService;

import java.util.List;

public class GlobalAlbumBillboardCommand extends GlobalBillboardCommand {
    public GlobalAlbumBillboardCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public String getDescription() {
        return "The most popular albums last week on the whole bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalalbumbillboard", "globaltrendalbum", "galbtrend", "gtrendalb");
    }



    @Override
    public String getName() {
        return "Bot's Album Billboard Top 100";
    }

    @Override
    public String getTitle() {
        return "Albums ";
    }

    @Override
    public List<BillboardEntity> getEntities(int weekId, boolean doListeners) {
        return getService().getGlobalAlbumBillboard(weekId, doListeners);
    }
}
