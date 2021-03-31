package core.parsers;

import core.parsers.explanation.FullTimeframeExplanation;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.GayParams;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.GayType;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import javacutils.Pair;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.regex.Pattern;

public class GayParser extends ChartableParser<GayParams> {
    private final static Pattern gayRegex = Pattern.compile("((lgtb).*|(gay)|(bi)(?:sexual)?|(trans)(?:exual)?|(non( )?binary)|(nb)|(lesb)(ian)?|(ace|asexual))", Pattern.CASE_INSENSITIVE);

    public GayParser(ChuuService service, TimeFrameEnum defaultTimeFrame) {
        super(service, defaultTimeFrame);
    }

    @Override
    void setUpOptionals() {
        this.opts.add(new OptionalEntity("plays", "display play count"));
        this.opts.add(new OptionalEntity("titles", "display titles"));
        this.opts.add(new OptionalEntity("artist", "use artists instead of albums"));
    }

    @Override
    public GayParams parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        TimeFrameEnum timeFrame = this.defaultTFE;
        String substring = e.getMessage().getContentRaw().substring(1).split("\\s+")[0].toLowerCase();
        GayType type = switch (substring) {
            case "trans" -> GayType.TRANS;
            case "ace" -> GayType.ACE;
            case "nonbinary", "nb" -> GayType.NB;
            case "lesbian" -> GayType.LESBIAN;
            case "bi" -> GayType.BI;
            default -> null;
        };
        if (type == null) {
            Pair<String[], GayType> gayPair = filterMessage(subMessage, gayRegex.asMatchPredicate(), s1 -> {
                s1 = s1.toLowerCase();
                if (s1.startsWith("bi")) {
                    return GayType.BI;
                } else if (s1.startsWith("trans")) {
                    return GayType.TRANS;
                } else if (s1.startsWith("nb") || s1.equals("nonbinary") || s1.equals("non binary")) {
                    return GayType.NB;
                } else if (s1.startsWith("lesb")) {
                    return GayType.LESBIAN;
                } else if (s1.startsWith("ace") || s1.startsWith("asexual")) {
                    return GayType.ACE;
                }
                return GayType.LGTBQ;
            }, GayType.LGTBQ);
            subMessage = gayPair.first;
            type = gayPair.second;
        }
        int y = type.getColumns();

        ChartParserAux chartParserAux = new ChartParserAux(subMessage);
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        subMessage = chartParserAux.getMessage();
        Pair<String[], Integer> integerPair = filterMessage(subMessage, NumberParser.compile.asMatchPredicate(), Integer::parseInt, 5);
        subMessage = integerPair.first;
        int x = integerPair.second;
        LastFMData data = atTheEndOneUser(e, subMessage);
        return new GayParams(e, data, new CustomTimeFrame(timeFrame), x, y, x, type);

    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(() -> new ExplanationLine("[LGTBQ,BI,TRANS,NB,LESBIAN,ACE]", null), () -> new ExplanationLine("Number of columns", "If number of columns is not specified it defaults to 5 columns"), new FullTimeframeExplanation(defaultTFE), new PermissiveUserExplanation());
    }
}
