package core.apis.last;

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


	public String getArtistName() {
		return artistName;
	}


	public String getAlbumName() {
		return albumName;
	}

}
