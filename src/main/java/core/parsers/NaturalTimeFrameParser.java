package core.parsers;

import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NaturalTimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NaturalTimeFrameParser extends DaoParser {
    private final NaturalTimeFrameEnum defaultTFE;

    public NaturalTimeFrameParser(ChuuService dao, NaturalTimeFrameEnum defaultTFE) {
        super(dao);
        this.defaultTFE = defaultTFE;
        this.opts.add(new OptionalEntity("--hour", "use the scrobble rate based on hours instead of days"));
    }

    public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {

        String[] message = getSubMessage(e.getMessage());
        NaturalTimeFrameEnum timeFrame = defaultTFE;

        ChartParserAux auxiliar = new ChartParserAux(message);
        timeFrame = auxiliar.parseNaturalTimeFrame(timeFrame);
        message = auxiliar.getMessage();

        LastFMData data = getLastFmUsername1input(message, e.getAuthor().getIdLong(), e);

        return new String[]{data.getName(), String.valueOf(data.getDiscordId()), timeFrame.toApiFormat()};
    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *[d,w,m,q,s,y,a]* *username ** \n" +
               "\tIf timeframe is not specified it defaults to " + defaultTFE.toString() + "\n" +
               "\tIf username is not specified it defaults to authors account \n";
    }

}
