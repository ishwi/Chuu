package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.params.ChartParameters;
import core.parsers.params.RainbowParams;
import core.parsers.utils.OptionalEntity;
import core.parsers.utils.Optionals;
import dao.ChuuService;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

public class RainbowParser extends ChartableParser<RainbowParams> {
    private final ChartNormalParser inner;

    public RainbowParser(ChuuService dao, TimeFrameEnum defaultTFE, OptionalEntity... opts) {
        super(dao, defaultTFE, opts);
        inner = new ChartNormalParser(dao, defaultTFE);
    }


    @Override
    public RainbowParams parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        ChartParameters chartParameters = inner.parseSlashLogic(ctx);
        if (chartParameters == null) {
            return null;
        }
        return new RainbowParams(ctx, chartParameters.getUser(), chartParameters.getTimeFrameEnum(), chartParameters.getX(), chartParameters.getY());
    }

    @Override
    void setUpOptionals() {
        addOptional(Optionals.TITLES.opt,
                Optionals.PLAYS.opt,
                Optionals.ARTIST.opt,
                new OptionalEntity("linear", "display the rainbow line by line instead of stair"),
                new OptionalEntity("color", "sort by color instead of brightness"),
                new OptionalEntity("column", "display rainbow column by column instead of in stair"),
                new OptionalEntity("inverse", "show it black to white instead of white to black"));
    }

    @Override
    public RainbowParams parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {
        ChartParameters chartParameters = inner.parseLogic(e, subMessage);
        if (chartParameters == null) {
            return null;
        }
        return new RainbowParams(e, chartParameters.getUser(), chartParameters.getTimeFrameEnum(), chartParameters.getX(), chartParameters.getY());
    }


}
