package core.commands.uniques;

import core.commands.utils.CommandCategory;
import dao.ChuuService;
import dao.entities.TrackPlays;
import dao.entities.UniqueWrapper;

import java.util.Arrays;
import java.util.List;

public class GlobalUniquesTrackCommand extends UniqueSongCommand {
    public GlobalUniquesTrackCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = true;
        isLongRunningCommand = true;

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
    public UniqueWrapper<TrackPlays> getList(long ignored, String lastFmName) {
        return db.getGlobalTrackUniques(lastFmName);
    }

    @Override
    public String getDescription() {
        return "Your unique songs considering all bot users";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("globaltrackunique", "gtru", "gsongu");
    }

    @Override
    public String getName() {
        return "Global song uniques";
    }
}
