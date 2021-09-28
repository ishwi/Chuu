package core.commands.uniques;

import core.commands.utils.CommandCategory;
import dao.ServiceView;
import dao.entities.ArtistPlays;
import dao.entities.UniqueWrapper;

import java.util.Arrays;
import java.util.List;

public class GlobalUniquesCommand extends UniqueCommand {
    public GlobalUniquesCommand(ServiceView dao) {
        super(dao, true);
        this.respondInPrivate = true;

    }

    @Override
    public String slashName() {
        return "global-artists";
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.UNIQUES;
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
