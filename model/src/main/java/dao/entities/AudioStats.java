package dao.entities;

import dao.utils.Constants;

import java.text.DecimalFormat;
import java.util.Locale;

public enum AudioStats {


    HAPPINESS("measure from 0% to 100% describing the musical positiveness conveyed by a track. Tracks with high valence sound more positive (e.g. happy, cheerful, euphoric), while tracks with low valence sound more negative (e.g. sad, depressed, angry)."),
    SONG_LENGTH("Length of the song"),
    ENERGY("Represents a perceptual measure of intensity and activity. Typically, energetic tracks feel fast, loud, and noisy."),
    TEMPO("The overall estimated tempo of a track in beats per minute (BPM). In musical terminology, tempo is the speed or pace of a given piece and derives directly from the average beat duration."),
    ACOUSTICNESS("A measure of how acoustic this song is"),
    DANCEABILITY("Danceability describes how suitable a track is for dancing based on a combination of musical elements including tempo, rhythm stability, beat strength, and overall regularity"),
    INSTRUMENTALNESS("Predicts whether a track contains no vocals. The closer the instrumentalness value is to 100%, the greater likelihood the track contains no vocal content."),
    LIVENESS("Detects the presence of an audience in the recording. Higher liveness values represent an increased probability that the track was performed live."),
    LOUDNESS("The overall loudness of a track in decibels (dB). Loudness values are averaged across the entire track. Values typical range between 0 and 60 db."),
    SPEECHINESS("Speechiness detects the presence of spoken words in a track. The more exclusively speech-like the recording (e.g. talk show, audio book, poetry), the closer to 100%");

    private static final DecimalFormat df = new DecimalFormat("##.##%");
    private static final DecimalFormat db = new DecimalFormat("##.# 'dB' ");
    public final String description;

    AudioStats(String description) {
        this.description = description;
    }

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
