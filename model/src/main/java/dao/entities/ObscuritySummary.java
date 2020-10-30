package dao.entities;

public class ObscuritySummary {
	private int totalPlays;
	private int othersPlaysOnYourArtists;
	private int uniqueCoefficient;
	private int total;

	public ObscuritySummary(int totalPlays, int othersPlaysOnYourArtists, int uniqueCoefficient, int total) {
		this.totalPlays = totalPlays;
		this.othersPlaysOnYourArtists = othersPlaysOnYourArtists;
		this.uniqueCoefficient = uniqueCoefficient;
		this.total = total;
	}

	public int getTotalPlays() {
		return totalPlays;
	}

	public void setTotalPlays(int totalPlays) {
		this.totalPlays = totalPlays;
	}

	public int getOthersPlaysOnYourArtists() {
		return othersPlaysOnYourArtists;
	}

	public void setOthersPlaysOnYourArtists(int othersPlaysOnYourArtists) {
		this.othersPlaysOnYourArtists = othersPlaysOnYourArtists;
	}

	public int getUniqueCoefficient() {
		return uniqueCoefficient;
	}

	public void setUniqueCoefficient(int uniqueCoefficient) {
		this.uniqueCoefficient = uniqueCoefficient;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
}
