package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.params.TimeFrameParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TimerFrameParser extends DaoParser<TimeFrameParameters> {
    private final TimeFrameEnum defaultTFE;

    public TimerFrameParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao);
        this.defaultTFE = defaultTFE;
    }

    public TimeFrameParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {

        String[] message = getSubMessage(e.getMessage());
        TimeFrameEnum timeFrame = defaultTFE;

        ChartParserAux auxiliar = new ChartParserAux(message);
        timeFrame = auxiliar.parseTimeframe(timeFrame);
        message = auxiliar.getMessage();
        LastFMData data = atTheEndOneUser(e, message);
        return new TimeFrameParameters(e, data, timeFrame);
    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *[d,w,m,q,s,y,a]* *username*** \n" +
                "\tIf timeframe is not specified it defaults to " + defaultTFE.toString() + "\n" +
                "\tIf username is not specified it defaults to authors account \n";
    }

}
