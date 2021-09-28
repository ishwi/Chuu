package core.commands.billboard;

import core.Chuu;
import core.commands.Context;
import dao.ServiceView;
import dao.entities.BillboardEntity;

import java.util.List;

public class GlobalAlbumBillboardCommand extends GlobalBillboardCommand {
    public GlobalAlbumBillboardCommand(ServiceView dao) {
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
    public List<BillboardEntity> getEntities(int weekId, boolean doListeners, Context e) {
        List<BillboardEntity> globalAlbumBillboard = db.getGlobalAlbumBillboard(weekId, doListeners);
        globalAlbumBillboard.forEach(t -> t.setUrl(Chuu.getCoverService().getCover(t.getArtist(), t.getName(), t.getUrl(), e)));
        return globalAlbumBillboard;
    }
}
