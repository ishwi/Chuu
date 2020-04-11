package core.commands;

import core.parsers.ChartYearParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;

import java.util.Arrays;
import java.util.List;

public class MbizThisYearCommand extends MusicBrainzCommand {


    public MbizThisYearCommand(ChuuService dao) {
        super(dao);
        this.searchSpace = 1500;
    }

    @Override
    public ChartableParser<ChartYearParameters> getParser() {
        return new ChartYearParser(getService());
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
}
