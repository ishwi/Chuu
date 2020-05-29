package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.params.ColorChartParams;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.beryx.awt.color.ColorFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorChartParser extends ChartableParser<ColorChartParams> {
    public ColorChartParser(ChuuService service, TimeFrameEnum defaultTimeFrame) {
        super(service, defaultTimeFrame);
    }

    @Override
    void setUpOptionals() {
        this.opts.add(new OptionalEntity("--plays", "display play count"));
        this.opts.add(new OptionalEntity("--titles", "display titles"));
        this.opts.add(new OptionalEntity("--artist", "use artists instead of albums"));
        this.opts.add(new OptionalEntity("--linear", "sort line by line "));
        this.opts.add(new OptionalEntity("--column", "sort column by column"));
        this.opts.add(new OptionalEntity("--color", "sort by color"));
        this.opts.add(new OptionalEntity("--ordered", "sort by plays"));
        this.opts.add(new OptionalEntity("--strict", "reduce the error range to make the color more accurate"));
        this.opts.add(new OptionalEntity("--inverse", " inverse the color ordering on the sorts that use color"));
    }

    @Override
    public ColorChartParams parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
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

        List<String> remaining = new ArrayList<>();
        Set<Color> colorList = new HashSet<>();
        for (String s : subMessage) {
            try {
                Color color = ColorFactory.valueOf(s);
                colorList.add(color);
            } catch (IllegalArgumentException ex) {
                remaining.add(s);
            }
        }
        if (colorList.isEmpty()) {
            sendError("Was not able to obtain any colour.\nYou can get a colour by color name," +
                    " by hex code (starting with # or 0x) " +
                    "or any other valid HTML color constructor like rgb(0,0,0)", e);
            return null;
        }
        LastFMData data = atTheEndOneUser(e, remaining.toArray(String[]::new));
        return new ColorChartParams(e, data.getName(), data.getDiscordId(), timeFrame, x, y, colorList, data.getChartMode());
    }

    @Override
    public String getUsageLogic(String commandName) {
        Pattern compile = Pattern.compile("\\*\\*" + commandName + "(.*)\\*\\* ");
        String usageLogic = super.getUsageLogic(commandName);
        String[] split = usageLogic.split("\n");
        for (int i = 0; i < split.length; i++) {
            String input = split[i];
            Matcher matcher = compile.matcher(input);
            if (matcher.matches()) {
                split[i] = "**" + commandName + matcher.group(1) + " *colour***\n\tA colour can be introduced by name, hex code or other html constructs";
            }
        }
        return String.join("\n", split) + "\n";
    }
}
