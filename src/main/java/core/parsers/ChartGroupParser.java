package core.parsers;

import core.exceptions.LastFmException;
import core.parsers.params.ChartGroupParameters;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.TimeFrameEnum;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartGroupParser extends ChartableParser<ChartGroupParameters> {

    private final ChartNormalParser inner;

    public ChartGroupParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao, defaultTFE);
        this.inner = new ChartNormalParser(dao, defaultTFE);
        this.inner.addOptional(new OptionalEntity("notime", "dont display time spent"));
        this.opts.addAll(this.inner.opts);
    }

    @Override
    public ChartGroupParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        ChartParameters chartParameters;
        try {
            chartParameters = inner.parse(e);
            return new ChartGroupParameters(e, chartParameters.getUser(), chartParameters.getDiscordId(), chartParameters.getTimeFrameEnum(), chartParameters.getX(), chartParameters.getY(), !chartParameters.hasOptional("notime"), chartParameters.chartMode(), chartParameters.getLastFMData());

        } catch (LastFmException lastFmException) {
            throw new ChuuServiceException("Improvable Exception");
        }
    }
}
