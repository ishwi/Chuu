package core.commands;

import dao.DaoImplementation;
import dao.entities.UniqueData;
import dao.entities.UniqueWrapper;

import java.util.Arrays;
import java.util.List;

public class GlobalUniquesCommand extends UniqueCommand {
    public GlobalUniquesCommand(DaoImplementation dao) {
        super(dao);
        this.respondInPrivate = true;
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public UniqueWrapper<UniqueData> getList(long ignored, String lastFmName) {
        return getDao().getGlobalUniques(lastFmName);
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
