package core.apis.last.exceptions;

public class TrackException extends ExceptionEntity {
    private static final String FIELD = "track";
    private final String artist;
    private final String song;

    public TrackException(String userName, String artist, String song) {
        super(userName);
        this.artist = artist;
        this.song = song;
    }

    public TrackException(String artist, String song) {
        super(null);
        this.artist = artist;
        this.song = song;
    }

    @Override
    public String getField() {
        return FIELD;
    }

    public String getArtist() {
        return artist;
    }

    public String getSong() {
        return song;
    }

}
