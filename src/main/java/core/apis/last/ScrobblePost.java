package core.apis.last;

public final class ScrobblePost {
    private final String method;
    private final String artist;
    private final String track;
    private final String album;
    private final Integer trackNumber;
    private final String mbid;
    private final Long timestamp;
    private final Integer duration;
    private final String albumArtist;
    private final String api_key;
    private String api_sig;

    ScrobblePost(String method, String artist, String track, String album, Integer trackNumber, String mbid, Long timestamp, Integer duration, String albumArtist, String api_key) {
        this.method = method;
        this.artist = artist;
        this.track = track;
        this.album = album;
        this.trackNumber = trackNumber;
        this.mbid = mbid;
        this.timestamp = timestamp;
        this.duration = duration;
        this.albumArtist = albumArtist;
        this.api_key = api_key;
        this.api_sig = generateApiSig(this);
    }

    private String generateApiSig(core.apis.last.ScrobblePost scrobblePost) {

        return null;
    }

    public String artist() {
        return artist;
    }

    public String track() {
        return track;
    }

    public String album() {
        return album;
    }

    public int trackNumber() {
        return trackNumber;
    }

    public String mbid() {
        return mbid;
    }

    public int duration() {
        return duration;
    }

    public String albumArtist() {
        return albumArtist;
    }

    public String api_key() {
        return api_key;
    }

    public String api_sig() {
        return api_sig;
    }

    public void setApi_sig(String api_sig) {
        this.api_sig = api_sig;
    }
}
