package dao.utils;

import java.time.LocalDate;

public class Constants {
    public static final LocalDate LASTFM_CREATION_DATE = LocalDate.of(2002, 2, 20);

    public static String getTimestamp(long ms) {
        var s = ms / 1000;
        var m = s / 60;
        var h = m / 60;

        if (h > 0) {
            return String.format("%02d:%02d:%02d", h, m % 60, s % 60);
        } else {
            return String.format("%02d:%02d", m, s % 60);
        }
    }
}
