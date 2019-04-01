package DAO.Entities;

public class NowPlayingArtist {
	private String artistName;
	private String mbid;
	private boolean nowPlaying;
	private String albumName;
	private String songName;
	private String url;

	public NowPlayingArtist(String artistName, String mbid, boolean nowPlaying, String albumName, String songName, String url) {
		this.artistName = artistName;
		this.mbid = mbid;
		this.nowPlaying = nowPlaying;
		this.albumName = albumName;
		this.songName = songName;
		this.url = url;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getMbid() {
		return mbid;
	}

	public void setMbid(String mbid) {
		this.mbid = mbid;
	}

	public boolean isNowPlaying() {
		return nowPlaying;
	}

	public void setNowPlaying(boolean nowPlaying) {
		this.nowPlaying = nowPlaying;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}