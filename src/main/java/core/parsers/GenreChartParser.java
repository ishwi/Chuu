package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.params.ChartableGenreParameters;
import core.parsers.params.GenreParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenreChartParser extends ChartableParser<ChartableGenreParameters> {
    private final GenreParser innerParser;

    public GenreChartParser(ChuuService dao, TimeFrameEnum defaultTFE, ConcurrentLastFM lastFM) {
        super(dao, defaultTFE);
        innerParser = new GenreParser(dao, lastFM);
    }


    @Override
    public ChartableGenreParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
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
            return new ChartableGenreParameters(e, data.getName(), data.getDiscordId(), data.getChartMode(), new CustomTimeFrame(timeFrameEnum), x, y, genreParameters, data);
        } catch (LastFmException lastFmException) {
            throw new ChuuServiceException();
        }

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
                split[i] = "**" + commandName + matcher.group(1) + " *genre***\n\tA genre can be specified or otherwise it defaults to the genre of your current track\\album\\artist according to last.fm";
            }
        }
        return String.join("\n", split) + "\n";
    }
}
