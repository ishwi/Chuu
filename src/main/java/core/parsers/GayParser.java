package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.params.GayParams;
import dao.ChuuService;
import dao.entities.GayType;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import javacutils.Pair;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GayParser extends ChartableParser<GayParams> {
    private final static Pattern gayRegex = Pattern.compile("((lgtb).*|(gay)|(bi)(?:sexual)?|(trans)(?:exual)?)", Pattern.CASE_INSENSITIVE);

    public GayParser(ChuuService service, TimeFrameEnum defaultTimeFrame) {
        super(service, defaultTimeFrame);
    }

    @Override
    void setUpOptionals() {
        this.opts.add(new OptionalEntity("--plays", "display play count"));
        this.opts.add(new OptionalEntity("--titles", "display titles"));
        this.opts.add(new OptionalEntity("--artist", "use artists instead of albums"));
    }

    @Override
    public GayParams parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        TimeFrameEnum timeFrame = this.defaultTFE;

        Pair<String[], GayType> gayPair = filterMessage(subMessage, gayRegex.asMatchPredicate(), s1 -> {
            if (s1.startsWith("bi")) {
                return GayType.BI;
            } else if (s1.startsWith("trans")) {
                return GayType.TRANS;
            }
            return GayType.LGTBQ;
        }, GayType.LGTBQ);
        subMessage = gayPair.first;
        GayType gayType = gayPair.second;

        int y = gayType.getColumns();

        ChartParserAux chartParserAux = new ChartParserAux(subMessage);
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        subMessage = chartParserAux.getMessage();
        Pair<String[], Integer> integerPair = filterMessage(subMessage, NumberParser.compile.asMatchPredicate(), Integer::parseInt, 5);
        subMessage = integerPair.first;
        int x = integerPair.second;
        LastFMData data = atTheEndOneUser(e, subMessage);
        return new GayParams(e, data.getName(), data.getDiscordId(), gayType, timeFrame, y, x, data.getChartMode());

    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *[d,w,m,q,s,y,a]* *[LGTBQ,BI,TRANS]* *number_of_columns*  *Username* ** \n" +
                "\tIf time is not specified defaults to " + defaultTFE.name().toLowerCase() + "\n" +
                "\tIf username is not specified defaults to authors account \n" +
                "\tIf number of columns is not specified it defaults to 5 items per row\n";
    }
}
