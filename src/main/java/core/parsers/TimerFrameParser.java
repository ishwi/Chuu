package core.parsers;

import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TimerFrameParser extends DaoParser {
    private final TimeFrameEnum defaultTFE;

    public TimerFrameParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao);
        this.defaultTFE = defaultTFE;
    }

    public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {

        String[] message = getSubMessage(e.getMessage());
        TimeFrameEnum timeFrame = defaultTFE;

        ChartParserAux auxiliar = new ChartParserAux(message);
        timeFrame = auxiliar.parseTimeframe(timeFrame);
        message = auxiliar.getMessage();

        LastFMData data = getLastFmUsername1input(message, e.getAuthor().getIdLong(), e);

        return new String[]{data.getName(), String.valueOf(data.getDiscordId()), timeFrame.toApiFormat()};
    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *[w,m,q,s,y,a]* *username ** \n" +
               "\tIf timeframe is not specified it defaults to " + defaultTFE.toString() + "\n" +
               "\tIf username is not specified it defaults to authors account \n";
    }

}
