package core.parsers;

import core.commands.Context;
import core.exceptions.LastFmException;
import core.parsers.params.ChartGroupParameters;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.TimeFrameEnum;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.InstanceNotFoundException;

public class ChartGroupParser extends ChartableParser<ChartGroupParameters> {

    private final ChartNormalParser inner;

    public ChartGroupParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao, defaultTFE);
        this.inner = new ChartNormalParser(dao, defaultTFE);
        this.inner.addOptional(new OptionalEntity("notime", "dont display time spent"));
        this.opts.addAll(this.inner.opts);
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
