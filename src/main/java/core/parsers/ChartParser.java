package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.exceptions.InvalidDateException;
import core.parsers.explanation.FullTimeframeExplanation;
import core.parsers.explanation.TimeframeExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ChartParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.List;
import java.util.stream.Stream;

public class ChartParser extends ChartableParser<ChartParameters> {


    public ChartParser(ChuuService dao) {
        super(dao, TimeFrameEnum.WEEK);
    }

    @Override
    public ChartParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        CustomTimeFrame timeFrameEnum;
        try {
            timeFrameEnum = InteractionAux.parseCustomTimeFrame(e, this.defaultTFE);
        } catch (InvalidDateException invalidDateException) {
            this.sendError(invalidDateException.getErrorMessage(), ctx);
            return null;
        }
        User user = InteractionAux.parseUser(e);
        Point point = InteractionAux.parseSize(e, () -> sendError(getErrorMessage(6), ctx));
        if (point == null) {
            return null;
        }
        LastFMData data = findLastfmFromID(user, ctx);
        return new ChartParameters(ctx, data, timeFrameEnum, point.x, point.y);
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

    @Override
    public List<Explanation> getUsages() {
        return Stream.concat(super.getUsages().stream().filter(t -> !(t instanceof TimeframeExplanation)), Stream.of(new FullTimeframeExplanation(defaultTFE))).toList();
    }

}
