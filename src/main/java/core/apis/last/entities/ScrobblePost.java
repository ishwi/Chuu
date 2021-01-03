package core.apis.last.entities;

public final class ScrobblePost {
    public final String method;
    public final String artist;
    public final String track;
    public final String album;
    public final Integer trackNumber;
    public final String mbid;
    public final Long timestamp;
    public final Integer duration;
    public final String albumArtist;
    public final String api_key;
    public final String sk;
    public String api_sig;

    public ScrobblePost(String method, String artist, String track, String album, Integer trackNumber, String mbid, Long timestamp, Integer duration, String albumArtist, String api_key, String sk) {
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
        this.sk = sk;
        this.api_sig = generateApiSig(this);
    }

    public String generateApiSig(ScrobblePost scrobblePost) {

        return null;
    }


    public void setApi_sig(String api_sig) {
        this.api_sig = api_sig;
    }
}
