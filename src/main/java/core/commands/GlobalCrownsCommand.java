package core.commands;

import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.UniqueWrapper;

import java.util.Arrays;
import java.util.List;

public class GlobalCrownsCommand extends CrownsCommand {
    public GlobalCrownsCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = true;
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public UniqueWrapper<ArtistPlays> getList(NumberParameters<ChuuDataParams> params) {
        Long threshold = params.getExtraParam();
        long idLong = params.getE().getGuild().getIdLong();

        if (threshold == null) {
            if (params.getE().isFromGuild()) {
                threshold = (long) getService().getGuildCrownThreshold(idLong);
            } else {
                threshold = 0L;
            }
        }
        return getService().getGlobalCrowns(params.getInnerParams().getLastFMData().getName(), Math.toIntExact(threshold));
    }

    @Override
    public String getDescription() {
        return "Like your crowns but considering all bot users instead of only a server";
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
