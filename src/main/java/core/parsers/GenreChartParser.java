package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.explanation.GenreExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ChartableGenreParameters;
import core.parsers.params.GenreParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.List;
import java.util.stream.Stream;

public class GenreChartParser extends ChartableParser<ChartableGenreParameters> {
    private final GenreParser innerParser;

    public GenreChartParser(ChuuService dao, TimeFrameEnum defaultTFE, ConcurrentLastFM lastFM) {
        super(dao, defaultTFE);
        innerParser = new GenreParser(dao, lastFM);
    }

    @Override
    public ChartableGenreParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        GenreParameters genreParameters = innerParser.parseSlashLogic(ctx);
        if (genreParameters == null) {
            return null;
        }
        LastFMData data = genreParameters.getLastFMData();
        TimeFrameEnum timeFrameEnum = InteractionAux.parseTimeFrame(e, defaultTFE);
        Point point = InteractionAux.parseSize(e, () -> this.sendError(getErrorMessage(6), ctx));
        if (point == null) {
            return null;
        }
        return new ChartableGenreParameters(ctx, data, new CustomTimeFrame(timeFrameEnum), point.x, point.y, data.getChartMode(), genreParameters);
    }

    @Override
    public ChartableGenreParameters parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {
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
        TimeFrameEnum timeFrameEnum = chartParserAux.parseTimeframe(this.defaultTFE);
        subMessage = chartParserAux.getMessage();
        ParserAux parserAux = new ParserAux(subMessage);
        User oneUser = parserAux.getOneUser(e, dao);
        String[] message = parserAux.getMessage();
        LastFMData data = findLastfmFromID(oneUser, e);
        try {
            GenreParameters genreParameters = innerParser.parseLogic(e, message);
            if (genreParameters == null) {
                return null;
            }
            return new ChartableGenreParameters(e, data, new CustomTimeFrame(timeFrameEnum), x, y, data.getChartMode(), genreParameters);
        } catch (LastFmException lastFmException) {
            throw new ChuuServiceException(lastFmException);
        }

    }

    @Override
    public List<Explanation> getUsages() {
        return Stream.concat(Stream.of(new GenreExplanation()), super.getUsages().stream()).toList();
    }
}
