package core.commands.crowns;

import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.UniqueWrapper;

import java.util.Arrays;
import java.util.List;

public class GlobalTrackCrownsCommand extends CrownsCommand {
    public GlobalTrackCrownsCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = true;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public String getTitle() {
        return "global track ";
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
        return db.getGlobalTrackCrowns(params.getInnerParams().getLastFMData().getName(),
                Math.toIntExact(threshold),
                CommandUtil.showBottedAccounts(params.getInnerParams().getLastFMData(), params, db), params.getE().getAuthor().getIdLong());
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> initParser() {
        Parser<NumberParameters<ChuuDataParams>> parser = super.initParser();
        parser.addOptional(new OptionalEntity("nobotted", "discard users that have been manually flagged as potentially botted accounts"));
        parser.addOptional(new OptionalEntity("botted", "discard users that have been manually flagged as potentially botted accounts"));
        return parser;
    }

    @Override
    public String getDescription() {
        return "Like your track crowns but considering all bot users instead of only a server";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("globaltrackcrowns", "gtrc", "gtc");
    }

    @Override
    public String getName() {
        return "Global track crowns";
    }
}
