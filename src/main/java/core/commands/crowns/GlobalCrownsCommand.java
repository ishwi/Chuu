package core.commands.crowns;

import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import core.parsers.utils.Optionals;
import dao.ServiceView;
import dao.entities.ArtistPlays;
import dao.entities.UniqueWrapper;

import java.util.Arrays;
import java.util.List;

public class GlobalCrownsCommand extends CrownsCommand {
    public GlobalCrownsCommand(ServiceView dao) {
        super(dao);
        this.respondInPrivate = true;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public String getTitle() {
        return "global ";
    }

    @Override
    public String slashName() {
        return "global-artists";
    }

    @Override
    public UniqueWrapper<ArtistPlays> getList(NumberParameters<ChuuDataParams> params) {
        Long threshold = params.getExtraParam();
        if (threshold == null) {
            if (params.getE().isFromGuild()) {
                long idLong = params.getE().getGuild().getIdLong();
                threshold = (long) db.getGuildCrownThreshold(idLong);
            } else {
                threshold = 0L;
            }
        }
        return db.getGlobalCrowns(params.getInnerParams().getLastFMData().getName(),
                Math.toIntExact(threshold),
                CommandUtil.showBottedAccounts(params.getInnerParams().getLastFMData(), params, db)
                , params.getE().getAuthor().getIdLong());
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> initParser() {
        Parser<NumberParameters<ChuuDataParams>> parser = super.initParser();
        parser.addOptional(Optionals.NOBOTTED.opt);
        parser.addOptional(Optionals.BOTTED.opt);
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
