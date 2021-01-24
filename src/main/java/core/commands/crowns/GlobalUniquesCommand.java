package core.commands.crowns;

import core.commands.utils.CommandCategory;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.UniqueWrapper;

import java.util.Arrays;
import java.util.List;

public class GlobalUniquesCommand extends UniqueCommand {
    public GlobalUniquesCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = true;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public UniqueWrapper<ArtistPlays> getList(long ignored, String lastFmName) {
        return db.getGlobalUniques(lastFmName);
    }

    @Override
    public String getDescription() {
        return "Your unique top considering all bot users";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("globalunique", "gu");
    }

    @Override
    public String getName() {
        return "Global Uniques";
    }
}
