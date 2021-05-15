package core.parsers.params;

import core.commands.Context;

public class MinutesParameters extends CommandParameters {
    private final int minutes;
    private final int seconds;
    private final int hours;

    public MinutesParameters(Context e, int minutes, int seconds, int hours) {
        super(e);
        this.minutes = minutes;
        this.seconds = seconds;
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getHours() {
        return hours;
    }
}
