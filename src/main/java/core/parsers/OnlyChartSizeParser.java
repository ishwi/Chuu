package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.params.ChartSizeParameters;
import dao.ChuuService;
import dao.entities.ChartMode;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class OnlyChartSizeParser extends ChartableParser<ChartSizeParameters> {
    public OnlyChartSizeParser(ChuuService dao, TimeFrameEnum defaultT, OptionalEntity... optionalEntity) {
        super(dao, defaultT, optionalEntity);
    }

    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("--notitles", "dont display titles"));
        opts.add(new OptionalEntity("--plays", "display play count"));
        opts.add(new OptionalEntity("--list", "display it as an embed"));
        opts.add(new OptionalEntity("--pie", "display it as a chart pie"));
    }

    @Override

    protected void setUpErrorMessages() {
        errorMessages.put(1, "0 is not a valid value for a chart!");
    }


    @Override
    public ChartSizeParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException {

        ChartParserAux chartParserAux = new ChartParserAux(words);
        int x = 5;
        int y = 5;

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
        ChartMode guildEmbedConfig;
        LastFMData lastfmFromID = findLastfmFromID(e.getAuthor(), e);

        return new ChartSizeParameters(e, x, y, lastfmFromID.getChartMode(), lastfmFromID);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *sizeXsize*  *Username* ** \n";
    }
}
