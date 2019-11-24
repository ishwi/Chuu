package main.apis.last;

public class ExceptionEntity {
	private String userName;
	private String artistName;
	private String albumName;


	public ExceptionEntity(String artistName, String albumName) {
		this.artistName = artistName;
		this.albumName = albumName;
	}

	public ExceptionEntity(String artistName, boolean ignored) {
		this.artistName = artistName;
	}

	public ExceptionEntity(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}
}
