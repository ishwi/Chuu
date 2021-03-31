package core.apis.last.entities;

public final record ScrobblePost(String method, String artist, String track,
                                 String album, Integer trackNumber, String mbid,
                                 Long timestamp, Integer duration, String albumArtist,
                                 String api_key, String sk) implements PostEntity {


}
