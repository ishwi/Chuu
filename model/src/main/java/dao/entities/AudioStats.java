package dao.entities;

import dao.utils.Constants;

import java.text.DecimalFormat;
import java.util.Locale;

public enum AudioStats {


    ACOUSTICNESS, DANCEABILITY, ENERGY, INSTRUMENTALNESS, LIVENESS, LOUDNESS, SPEECHINESS, TEMPO, HAPPINESS, SONG_LENGTH;

    private static final DecimalFormat df = new DecimalFormat("##.##%");
    private static final DecimalFormat db = new DecimalFormat("##.# 'dB' ");


    public String toValue(float value) {
        return switch (this) {
            case ACOUSTICNESS, DANCEABILITY, ENERGY, INSTRUMENTALNESS, LIVENESS, SPEECHINESS, HAPPINESS -> df.format(value);
            case LOUDNESS -> db.format(value);
            case TEMPO -> Math.round(value) + " BPM";
            case SONG_LENGTH -> Constants.getTimestamp((long) (value * 1000));
        };
    }

    public String getDbField() {
        if (this == AudioStats.HAPPINESS) {
            return "valence";
        }
        return this.name().toLowerCase(Locale.ROOT);
    }
}
