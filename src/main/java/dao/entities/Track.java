package dao.entities;

public class Track {
	private final String artist;
	private final String name;
	private final int plays;
	private final boolean isLoved;
	private final int duration;
	private int position;
	private String imageUrl;

	public Track(String artist, String name, int plays, boolean isLoved, int duration) {
		this.artist = artist;
		this.name = name;
		this.plays = plays;
		this.isLoved = isLoved;
		this.duration = duration;

	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getArtist() {
		return artist;
	}

	public String getName() {
		return name;
	}

	public int getPlays() {
		return plays;
	}

	public boolean isLoved() {
		return isLoved;
	}

	public int getDuration() {
		return duration;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public int hashCode() {
		int result = artist.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Track track = (Track) o;

		if (!artist.equals(track.artist)) return false;
		return name.equals(track.name);
	}

	@Override
	public String toString() {
		return ". **" + artist + " - " + name + "** - " + plays + " plays" +
				"\n";
	}
}
