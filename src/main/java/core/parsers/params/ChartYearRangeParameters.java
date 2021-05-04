package core.parsers.params;

import core.commands.Context;
import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;

import java.time.Year;
import java.time.temporal.ChronoUnit;

import static core.commands.utils.CommandUtil.getDecade;

public class ChartYearRangeParameters extends ChartParameters {
    private final Year baseYear;
    private final int numberOfYears;


    public ChartYearRangeParameters(Context e, LastFMData lastFMData, CustomTimeFrame timeFrameEnum, int x, int y, Year baseYear, int numberOfYears) {
        super(e, lastFMData, timeFrameEnum, x, y);
        this.baseYear = baseYear;
        this.numberOfYears = numberOfYears;
    }

    public ChartYearRangeParameters(Context e, LastFMData user, CustomTimeFrame timeFrameEnum, int x, int y, boolean writeTitles, boolean writePlays, boolean isList, Year baseYear, int numberOfYears) {
        super(e, user, timeFrameEnum, x, y, writeTitles, writePlays, isList);
        this.baseYear = baseYear;
        this.numberOfYears = numberOfYears;
    }

    public Year getBaseYear() {
        return baseYear;
    }

    public int getNumberOfYears() {
        return numberOfYears;
    }

    public int getLimitYear() {
        return getBaseYear().plus(numberOfYears, ChronoUnit.YEARS).getValue();
    }


    public String getDisplayString() {
        if (numberOfYears == 10 && baseYear.isAfter(Year.now().minus(100, ChronoUnit.YEARS))) {
            return "the " + getDecade(baseYear.getValue()) + "s";
        } else {
            return baseYear.toString() + " to " + getLimitYear();
        }
    }

    public boolean isByTime() {
        return hasOptional("time");
    }

    public boolean isCareAboutSized() {
        return !hasOptional("nolimit");
    }
}
