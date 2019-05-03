package DAO.Entities;

public class ArtistInfo {
	private String artistUrl;
	private String artistName;

	public ArtistInfo(String artistUrl, String artistName) {
		this.artistUrl = artistUrl;
		this.artistName = artistName;
	}

	public String getArtistUrl() {
		return artistUrl;
	}

	public void setArtistUrl(String artistUrl) {
		this.artistUrl = artistUrl;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
}
