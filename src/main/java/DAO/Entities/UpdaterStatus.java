package DAO.Entities;

public class UpdaterStatus {
	private String artistUrl;
	private String correction;

	//Url -> null
	public UpdaterStatus(String url, String correction) {
		this.correction = correction;
		this.artistUrl = url;
	}

	public String getArtistUrl() {
		return artistUrl;
	}

	public void setArtistUrl(String artistUrl) {
		this.artistUrl = artistUrl;
	}

	public boolean isNeedsUrlUpdate() {
		return artistUrl == null;
	}


	public String getCorrection() {
		return correction;
	}

	public void setCorrection(String correction) {
		this.correction = correction;
	}
}
