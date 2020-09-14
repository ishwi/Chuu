package core.commands;

import core.parsers.OptionalEntity;
import core.parsers.Parser;
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
    protected CommandCategory getCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public UniqueWrapper<ArtistPlays> getList(NumberParameters<ChuuDataParams> params) {
        Long threshold = params.getExtraParam();
        if (threshold == null) {
            if (params.getE().isFromGuild()) {
                long idLong = params.getE().getGuild().getIdLong();
                threshold = (long) getService().getGuildCrownThreshold(idLong);
            } else {
                threshold = 0L;
            }
        }
        return getService().getGlobalCrowns(params.getInnerParams().getLastFMData().getName(),
                Math.toIntExact(threshold),
                !params.hasOptional("nobotted"), params.getE().getAuthor().getIdLong());
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> getParser() {
        Parser<NumberParameters<ChuuDataParams>> parser = super.getParser();
        parser.addOptional(new OptionalEntity("nobotted", "discard users that have been manually flagged as potentially botted accounts"));
        return parser;
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
