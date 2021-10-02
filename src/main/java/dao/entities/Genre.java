package dao.entities;

public class Genre {
	private String genreName;
	private String representativeArtist;

	public Genre(String genreName, String representativeArtist) {
		this.genreName = genreName;
		this.representativeArtist = representativeArtist;
	}

	public String getGenreName() {
		return genreName;
	}

	public void setGenreName(String genreName) {
		this.genreName = genreName;
	}

	public String getRepresentativeArtist() {
		return representativeArtist;
	}

	public void setRepresentativeArtist(String representativeArtist) {
		this.representativeArtist = representativeArtist;
	}
}
