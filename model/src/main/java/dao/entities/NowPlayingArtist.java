package dao.entities;

public record NowPlayingArtist(String artistName, String artistMbid, boolean current,
                               String albumName, String songName, String url,
                               String username, boolean loved) {

}
