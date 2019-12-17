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

	@Override
	public int hashCode() {
		int result = genreName.hashCode();
		result = 31 * result + representativeArtist.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Genre genre = (Genre) o;

		if (!genreName.equals(genre.genreName)) return false;
		return representativeArtist.equals(genre.representativeArtist);
	}
}
