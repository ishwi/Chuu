package core.apis.last.exceptions;

public class ArtistException extends ExceptionEntity {
    private static final String FIELD = "artist";
    private final String artist;

    public ArtistException(String userName, String artist) {
        super(userName);
        this.artist = artist;
    }

    public ArtistException(String artist) {
        super(null);
        this.artist = artist;
    }

    @Override
    public String getField() {
        return FIELD;
    }

    public String getArtist() {
        return artist;
    }
}
