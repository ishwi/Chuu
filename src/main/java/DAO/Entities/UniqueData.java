package DAO.Entities;

public class UniqueData {
	private final int count;
	private final String artistName;

	public UniqueData(String name, int count_a) {
		this.artistName = name;
		this.count = count_a;
	}

	public String getArtistName() {
		return artistName;
	}

	public int getCount() {
		return count;
	}
}
