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

	@Override
	public String toString() {
		StringBuilder a = new StringBuilder();
		a.append(". **[")
				.append(getArtistName())
				.append("](https://www.last.fm/music/")
				.append(getArtistName().replaceAll(" ", "+"))
				.append(")** - ").append(getCount())
				.append(" plays\n");
		return a.toString();
	}
}
