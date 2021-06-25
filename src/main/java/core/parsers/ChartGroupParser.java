package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.params.ChartGroupParameters;
import core.parsers.params.ChartParameters;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.TimeFrameEnum;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.InstanceNotFoundException;

public class ChartGroupParser extends ChartableParser<ChartGroupParameters> {

    private final ChartNormalParser inner;

    public ChartGroupParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao, defaultTFE);
        this.inner = new ChartNormalParser(dao, defaultTFE);
        this.inner.addOptional((new OptionalEntity("notime", "not display time spent")));
        this.inner.getOptionals().forEach(this::addOptional);
    }

    @Override
    public ChartGroupParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        ChartParameters chartParameters;
        try {
            chartParameters = inner.parse(ctx);
            if (chartParameters == null) {
                return null;
            }
            return new ChartGroupParameters(ctx, chartParameters.getUser(), chartParameters.getTimeFrameEnum(), chartParameters.getX(), chartParameters.getY(), !chartParameters.hasOptional("notime"));

        } catch (LastFmException lastFmException) {
            throw new ChuuServiceException("Improvable Exception");
        }
    }

    @Override
    public ChartGroupParameters parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {
        ChartParameters chartParameters;
        try {
            chartParameters = inner.parse(e);
            if (chartParameters == null) {
                return null;
            }
            return new ChartGroupParameters(e, chartParameters.getUser(), chartParameters.getTimeFrameEnum(), chartParameters.getX(), chartParameters.getY(), !chartParameters.hasOptional("notime"));

        } catch (LastFmException lastFmException) {
            throw new ChuuServiceException("Improvable Exception");
        }
    }
}
