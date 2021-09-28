package core.commands.billboard;

import core.Chuu;
import core.commands.Context;
import dao.ServiceView;
import dao.entities.BillboardEntity;

import java.util.List;

public class BillboardAlbumCommand extends BillboardCommand {
    public BillboardAlbumCommand(ServiceView dao) {
        super(dao);
    }


    @Override
    public String getDescription() {
        return "The most popular albums last week on this server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("albumbillboard", "trendalbum", "albtrend", "trendalb");
    }

    @Override
    public String getName() {
        return "Server's Album Billboard Top 100";
    }

    @Override
    public String getTitle() {
        return "Albums ";
    }

    @Override
    public List<BillboardEntity> getEntities(int weekId, long guildId, boolean doListeners, Context e) {
        List<BillboardEntity> albumBillboard = db.getAlbumBillboard(weekId, guildId, doListeners);
        albumBillboard.forEach(t -> t.setUrl(Chuu.getCoverService().getCover(t.getArtist(), t.getName(), t.getUrl(), e)));
        return albumBillboard;
    }
}
