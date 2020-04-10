package core.apis.last.exceptions;

public class AlbumException extends ExceptionEntity {
    private static final String FIELD = "album";
    private final String artist;
    private final String album;

    public AlbumException(String userName, String artist, String album) {
        super(userName);
        this.artist = artist;
        this.album = album;
    }

    public AlbumException(String artist, String album) {
        super(null);
        this.artist = artist;
        this.album = album;
    }

    @Override
    public String getField() {
        return FIELD;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }
}
