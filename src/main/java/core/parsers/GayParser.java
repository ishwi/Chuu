package core.parsers;

import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.TimeframeExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.GayParams;
import core.parsers.utils.CustomTimeFrame;
import core.parsers.utils.Optionals;
import dao.ChuuService;
import dao.entities.GayType;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class GayParser extends ChartableParser<GayParams> {
    private final static Pattern gayRegex = Pattern.compile("((lgtb).*|(gay)|(bi)(?:sexual)?|(trans)(?:exual)?|(non( )?binary)|(nb)|(enby)|(lesb)(ian)?|(ace|asexual))", Pattern.CASE_INSENSITIVE);

    public GayParser(ChuuService service, TimeFrameEnum defaultTimeFrame) {
        super(service, defaultTimeFrame);
    }

    @Override
    void setUpOptionals() {
        addOptional(Optionals.PLAYS.opt, Optionals.TITLES.opt, Optionals.ARTIST.opt);
    }

    @Override
    public GayParams parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        CommandInteraction e = ctx.e();
        User user = InteractionAux.parseUser(e);
        TimeFrameEnum timeFrameEnum = InteractionAux.parseTimeFrame(e, defaultTFE);
        GayType flag = GayType.valueOf(e.getOption("flag").getAsString());
        int x = Math.toIntExact(Optional.ofNullable(e.getOption("columns")).map(OptionMapping::getAsLong).orElse(5L));
        return new GayParams(ctx, findLastfmFromID(user, ctx), CustomTimeFrame.ofTimeFrameEnum(timeFrameEnum), x, flag.getColumns(), x, flag);
    }

    @Override
    public GayParams parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {
        TimeFrameEnum timeFrame = this.defaultTFE;
        GayType type = null;
        if (e instanceof ContextMessageReceived mes) {
            String substring = mes.e().getMessage().getContentRaw().substring(1).split("\\s+")[0].toLowerCase();
            type = switch (substring) {
                case "trans" -> GayType.TRANS;
                case "ace" -> GayType.ACE;
                case "nonbinary", "nb", "enby" -> GayType.NB;
                case "lesbian" -> GayType.LESBIAN;
                case "bi" -> GayType.BI;
                default -> null;
            };
        }
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
            subMessage = gayPair.getLeft();
            type = gayPair.getRight();
        }
        int y = type.getColumns();

        ChartParserAux chartParserAux = new ChartParserAux(subMessage);
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        subMessage = chartParserAux.getMessage();
        Pair<String[], Integer> integerPair = filterMessage(subMessage, ParserAux.digitMatcher.asMatchPredicate(), Integer::parseInt, 5);
        subMessage = integerPair.getLeft();
        int x = integerPair.getRight();
        LastFMData data = atTheEndOneUser(e, subMessage);
        return new GayParams(e, data, new CustomTimeFrame(timeFrame), x, y, x, type);

    }

    @Override
    public List<Explanation> getUsages() {
        OptionData optionData = new OptionData(OptionType.STRING, "flag", "Flag to use").setRequired(true);
        for (GayType value : GayType.values()) {
            optionData.addChoice(value.name(), value.name());
        }
        return List.of(() -> new ExplanationLine("[LGTBQ,BI,TRANS,NB,LESBIAN,ACE]", null, optionData), () -> new ExplanationLineType("columns", "If number of columns is not specified it defaults to 5 columns", OptionType.INTEGER), new TimeframeExplanation(defaultTFE), new PermissiveUserExplanation());
    }
}
