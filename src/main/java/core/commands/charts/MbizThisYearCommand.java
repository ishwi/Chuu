package core.commands.charts;

import core.parsers.ChartSmartYearParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;

import java.util.Arrays;
import java.util.List;

public class MbizThisYearCommand extends MusicBrainzCommand {
    private static final int CUSTOM_SEARCH_SPACE = 2000;

    public MbizThisYearCommand(ChuuService dao) {
        super(dao);
        this.searchSpace = CUSTOM_SEARCH_SPACE;
    }

    @Override
    public ChartableParser<ChartYearParameters> initParser() {
        return new ChartSmartYearParser(db);
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
