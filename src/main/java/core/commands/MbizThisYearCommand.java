package core.commands;

import core.parsers.ChartFromYearVariableParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

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
    public ChartParameters getParameters(String[] message, MessageReceivedEvent e) {
        return new ChartYearParameters(message, e);
    }


}
