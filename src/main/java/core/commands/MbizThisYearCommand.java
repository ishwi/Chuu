package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ChartFromYearVariableParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.TimeFrameEnum;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class MbizThisYearCommand extends MusicBrainzCommand {


    public MbizThisYearCommand(ChuuService dao) {
        super(dao);
        this.parser = new ChartFromYearVariableParser(dao);
        this.searchSpace = 1500;
    }

    @Override
    public boolean doDiscogs() {
        return false;
    }


    @Override
    public String getDescription() {
        return "Gets your top albums of the year queried.\t" +
               "NOTE: The further the year is from the  current year, the less precise the command will be";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("aoty", "albumoftheyear");
    }

    @Override
    public String getName() {
        return "Albums Of The Year!";
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned;
        returned = parser.parse(e);
        if (returned == null)
            return;


        int x = Integer.parseInt(returned[0]);
        int y = Integer.parseInt(returned[1]);
        Year year = Year.of(Integer.parseInt(returned[2]));
        String username = returned[3];
        long discordId = Long.parseLong(returned[4]);
        boolean titleWrite = !Boolean.parseBoolean(returned[5]);
        boolean playsWrite = Boolean.parseBoolean(returned[6]);
        boolean noLimitFlag = Boolean.parseBoolean(returned[7]);
        boolean isList = Boolean.parseBoolean(returned[8]);


        TimeFrameEnum timeframe;
        LocalDateTime time = LocalDateTime.now();
        if (year.isBefore(Year.of(time.getYear()))) {
            timeframe = TimeFrameEnum.ALL;
        } else {
            int monthValue = time.getMonthValue();
            if (monthValue == 1 && time.getDayOfMonth() < 8) {
                timeframe = TimeFrameEnum.WEEK;
            } else if (monthValue < 2) {
                timeframe = TimeFrameEnum.MONTH;
            } else if (monthValue < 4) {
                timeframe = TimeFrameEnum.QUARTER;
            } else if (monthValue < 7)
                timeframe = TimeFrameEnum.SEMESTER;
            else {
                timeframe = TimeFrameEnum.YEAR;
            }
        }
        ChartYearParameters chartParameters = new ChartYearParameters(username, discordId, timeframe, x, y, e, titleWrite, playsWrite, isList, year, !noLimitFlag);
        CountWrapper<BlockingQueue<UrlCapsule>> result = processQueue(chartParameters);
        BlockingQueue<UrlCapsule> queue = result.getResult();
        if (isList) {
            ArrayList<UrlCapsule> liste = new ArrayList<>(queue.size());
            queue.drainTo(liste);
            doList(liste, chartParameters, result.getRows());
        } else {
            if (noLimitFlag) {
                int imageSize = (int) Math.ceil(Math.sqrt(queue.size()));
                doImage(queue, imageSize, imageSize, chartParameters);
            } else {
                BlockingQueue<UrlCapsule> tempQueuenew = new LinkedBlockingDeque<>();
                queue.drainTo(tempQueuenew, x * y);
                doImage(tempQueuenew, x, y, chartParameters);
            }
        }
    }

    @Override
    public void noElementsMessage(MessageReceivedEvent e, ChartParameters parameters) {
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        ChartYearParameters parmas = (ChartYearParameters) parameters;
        sendMessageQueue(e, String.format("Couldn't find any %s album in %s top %d albums%s!", parmas.getYear().toString(), ingo.getUsername(), searchSpace, parameters.getTimeFrameEnum().getDisplayString()));
    }
}
