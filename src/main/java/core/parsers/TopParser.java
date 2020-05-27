package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.params.TopParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class TopParser extends ChartableParser<TopParameters> {
    public TopParser(ChuuService dao) {
        super(dao, TimeFrameEnum.ALL);
    }

    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("--notitles", "don't display titles"));
        opts.add(new OptionalEntity("--noplays", " don't display play count"));
        opts.add(new OptionalEntity("--list", "display it as an embed"));
        opts.add(new OptionalEntity("--pie", "display it as a chart pie"));
        opts.add(new OptionalEntity("--album", "use artists instead of albums"));
    }

    @Override
    public TopParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
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
        subMessage = chartParserAux.getMessage();

        LastFMData data = atTheEndOneUser(e, subMessage);
        return new TopParameters(e, data.getName(), data.getDiscordId(), defaultTFE, x, y, data.getChartMode());
    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *sizeXsize*  *Username* ** \n" +
                "\tIf username is not specified defaults to authors account \n" +
                "\tIf Size not specified it defaults to 5x5\n";

    }
}
