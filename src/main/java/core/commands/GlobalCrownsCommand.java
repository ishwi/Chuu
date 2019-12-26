package core.commands;

import dao.DaoImplementation;
import dao.entities.UniqueData;
import dao.entities.UniqueWrapper;

import java.util.Arrays;
import java.util.List;

public class GlobalCrownsCommand extends CrownsCommand {
    public GlobalCrownsCommand(DaoImplementation dao) {
        super(dao);
        this.respondInPrivate = true;
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public UniqueWrapper<UniqueData> getList(long ignored, String lastFmName) {
        return getDao().getGlobalCrowns(lastFmName);
    }

    @Override
    public String getDescription() {
        return "Your top considering all bot users";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("globalcrowns", "gc");
    }

    @Override
    public String getName() {
        return "Global Crowns";
    }
}
