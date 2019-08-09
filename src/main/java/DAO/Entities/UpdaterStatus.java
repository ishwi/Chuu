package DAO.Entities;

public class UpdaterStatus {
	private final boolean correction_status;
	private String artistUrl;
	private String correction;

	//Url -> null
	public UpdaterStatus(String url, String correction, boolean correction_status) {
		this.correction = correction;
		this.artistUrl = url;
		this.correction_status = correction_status;
	}

	public String getArtistUrl() {
		return artistUrl;
	}

	public void setArtistUrl(String artistUrl) {
		this.artistUrl = artistUrl;
	}

	public String getCorrection() {
		return correction;
	}

	public void setCorrection(String correction) {
		this.correction = correction;
	}

	public boolean isCorrection_status() {
		return correction_status;
	}

}
