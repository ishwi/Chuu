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
}
