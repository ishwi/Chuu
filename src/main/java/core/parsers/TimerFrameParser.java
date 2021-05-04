package core.parsers;

import core.commands.Context;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.TimeframeExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.TimeFrameParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;

import java.util.List;

public class TimerFrameParser extends DaoParser<TimeFrameParameters> {
    private final TimeFrameEnum defaultTFE;

    public TimerFrameParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao);
        this.defaultTFE = defaultTFE;
    }

    public TimeFrameParameters parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {


        ChartParserAux auxiliar = new ChartParserAux(subMessage);
        TimeFrameEnum timeFrame = auxiliar.parseTimeframe(defaultTFE);
        subMessage = auxiliar.getMessage();
        LastFMData data = atTheEndOneUser(e, subMessage);
        return new TimeFrameParameters(e, data, timeFrame);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new TimeframeExplanation(defaultTFE), new PermissiveUserExplanation());
    }


}
