package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.explanation.ChartSizeExplanation;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ChartSizeParameters;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.List;

public class OnlyChartSizeParser extends ChartableParser<ChartSizeParameters> {
    public OnlyChartSizeParser(ChuuService dao, OptionalEntity... optionalEntity) {
        super(dao, TimeFrameEnum.ALL, optionalEntity);
    }


    @Override

    protected void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(1, "0 is not a valid value for a chart!");
    }

    @Override
    public ChartSizeParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        Point point = InteractionAux.parseSize(e, () -> this.sendError(getErrorMessage(6), ctx));
        if (point == null) {
            return null;
        }
        User user = InteractionAux.parseUser(e);
        LastFMData data = findLastfmFromID(user, ctx);
        return new ChartSizeParameters(ctx, point.x, point.y, data);
    }

    @Override
    public ChartSizeParameters parseLogic(Context e, String[] words) throws InstanceNotFoundException {

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

        LastFMData lastFMData = atTheEndOneUser(e, chartParserAux.getMessage());

        return new ChartSizeParameters(e, x, y, lastFMData);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new ChartSizeExplanation(), new PermissiveUserExplanation());
    }

}
