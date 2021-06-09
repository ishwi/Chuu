package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.explanation.ColorExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ColorChartParams;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.apache.commons.lang3.tuple.Pair;
import org.beryx.awt.color.ColorFactory;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ColorChartParser extends ChartableParser<ColorChartParams> {
    public ColorChartParser(ChuuService service, TimeFrameEnum defaultTimeFrame) {
        super(service, defaultTimeFrame);
    }

    @Override
    public ColorChartParams parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        User user = InteractionAux.parseUser(e);
        Point point = InteractionAux.parseSize(e, () -> this.sendError(getErrorMessage(6), ctx));
        if (point == null) {
            return null;
        }
        TimeFrameEnum timeFrameEnum = InteractionAux.parseTimeFrame(e, defaultTFE);
        Pair<Set<Color>, List<String>> parser = parser(Optional.ofNullable(e.getOption(ColorExplanation.NAME))
                .map(OptionMapping::getAsString)
                .map(z -> z.split("\\s+"))
                .orElse(new String[]{}), ctx);
        if (parser == null) {
            return null;
        }
        return new ColorChartParams(ctx, findLastfmFromID(user, ctx), CustomTimeFrame.ofTimeFrameEnum(timeFrameEnum), point.x, point.y, parser.getLeft());
    }

    @Override
    void setUpOptionals() {
        this.opts.add(new OptionalEntity("plays", "display play count"));
        this.opts.add(new OptionalEntity("titles", "display titles"));
        this.opts.add(new OptionalEntity("artist", "use artists instead of albums"));
        this.opts.add(new OptionalEntity("linear", "sort line by line "));
        this.opts.add(new OptionalEntity("column", "sort column by column"));
        this.opts.add(new OptionalEntity("color", "sort by color"));
        this.opts.add(new OptionalEntity("ordered", "sort by plays"));
        this.opts.add(new OptionalEntity("strict", "reduce the error range to make the color more accurate"));
        this.opts.add(new OptionalEntity("inverse", " inverse the color ordering on the sorts that use color"));
    }

    @Override
    public ColorChartParams parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {
        TimeFrameEnum timeFrame = this.defaultTFE;
        int x = 5;
        int y = 5;

        ChartParserAux chartParserAux = new ChartParserAux(subMessage);
        try {
            Point chartSize = chartParserAux.getChartSize();
            if (chartSize != null) {
                x = chartSize.x;
                y = chartSize.y;
            }
        } catch (InvalidChartValuesException ex) {
            this.sendError(getErrorMessage(6), e);
            return null;
        }
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        subMessage = chartParserAux.getMessage();
        var parser = parser(subMessage, e);
        if (parser == null) {
            return null;
        }

        LastFMData data = atTheEndOneUser(e, parser.getRight().toArray(String[]::new));
        return new ColorChartParams(e, data, CustomTimeFrame.ofTimeFrameEnum(timeFrame), x, y, parser.getLeft());
    }

    public Pair<Set<Color>, List<String>> parser(String[] words, Context ctx) {
        List<String> remaining = new ArrayList<>();
        Set<Color> colorList = new HashSet<>();
        for (String s : words) {
            try {
                Pattern compile = Pattern.compile("[0-9a-fA-F]+");
                if (compile.matcher(s).matches()) {
                    s = "#" + s;
                }
                Color color = ColorFactory.valueOf(s);
                colorList.add(color);
            } catch (IllegalArgumentException ex) {
                remaining.add(s);
            }
        }
        if (colorList.isEmpty()) {
            sendError("Was not able to obtain any colour.\nYou can get a colour by color name," +
                      " by hex code (starting with # or 0x) " +
                      "or any other valid HTML color constructor like rgb(0,0,0)", ctx);
            return null;
        }
        return Pair.of(colorList, remaining);
    }

    @Override
    public List<Explanation> getUsages() {
        return Stream.concat(Stream.of(new ColorExplanation()), super.getUsages().stream()).toList();
    }


}
