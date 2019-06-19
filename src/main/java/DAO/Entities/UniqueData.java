package DAO.Entities;

public class UniqueData {
	private final int count;
	private final String artistName;

	public UniqueData(String name, int count_a) {
		this.artistName = name;
		this.count = count_a;
	}

	private String getArtistName() {
		return artistName;
	}

	private int getCount() {
		return count;
	}

	@Override
	public String toString() {
		String a = ". [" +
				getArtistName() +
				"](https://www.last.fm/music/" +
				getArtistName().replaceAll(" ", "+") +
				") - " + getCount() +
				" plays\n";
		return a;
	}
}
