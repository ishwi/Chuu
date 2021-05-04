package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.exceptions.InvalidDateException;
import core.parsers.explanation.ChartSizeExplanation;
import core.parsers.explanation.FullTimeframeExplanation;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.params.ChartParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.Optional;

public class ChartParser extends ChartableParser<ChartParameters> {


    public ChartParser(ChuuService dao) {
        super(dao, TimeFrameEnum.WEEK);
    }

    @Override
    public ChartParameters parseSlashLogic(ContextSlashReceived e) throws LastFmException, InstanceNotFoundException {
        TimeFrameEnum timeFrameEnum = Optional.ofNullable(e.e().getOption(FullTimeframeExplanation.NAME)).map(SlashCommandEvent.OptionData::getAsString).map(TimeFrameEnum::get).orElse(this.defaultTFE);
        String size = Optional.ofNullable(e.e().getOption(ChartSizeExplanation.NAME)).map(SlashCommandEvent.OptionData::getAsString).orElse("5x5");
        User user = Optional.ofNullable(e.e().getOption(PermissiveUserExplanation.NAME)).map(SlashCommandEvent.OptionData::getAsUser).orElse(e.getAuthor());
        int x = 5;
        int y = 5;
        try {
            Point chartSize = ChartParserAux.processString(size);
        } catch (InvalidChartValuesException ex) {
            this.sendError(getErrorMessage(6), e);
            return null;
        }
        LastFMData data = findLastfmFromID(user, e);
        return new ChartParameters(e, data, CustomTimeFrame.ofTimeFrameEnum(timeFrameEnum), x, y);
    }

    @Override
    public ChartParameters parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {
        int x = 5;
        int y = 5;


        ParserAux parserAux = new ParserAux(subMessage);
        User oneUser = parserAux.getOneUser(e, dao);
        subMessage = parserAux.getMessage();
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
        CustomTimeFrame timeFrame;
        try {
            timeFrame = chartParserAux.parseCustomTimeFrame(defaultTFE);
        } catch (InvalidDateException invalidDateException) {
            this.sendError(invalidDateException.getErrorMessage(), e);
            return null;
        }
        subMessage = chartParserAux.getMessage();
        LastFMData data;
        if (!oneUser.equals(e.getAuthor())) {
            data = findLastfmFromID(oneUser, e);
        } else {
            data = atTheEndOneUser(e, subMessage);
        }
        return new ChartParameters(e, data, timeFrame, x, y);
    }

}
