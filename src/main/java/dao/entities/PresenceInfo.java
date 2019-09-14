package dao.entities;

public class PresenceInfo {
	private final String artist;
	private final String url;
	private final long sum;
	private long discordId;

	public PresenceInfo(String artist_id, String url, long summa, long discordID) {
		this.artist = artist_id;
		this.url = url;
		this.sum = summa;
		this.discordId = discordID;
	}

	public PresenceInfo(String artist_id, String url, long summa) {
		this.artist = artist_id;
		this.url = url;
		this.sum = summa;
	}

	public String getArtist() {
		return artist;
	}

	public String getUrl() {
		return url;
	}

	public long getSum() {
		return sum;
	}

	public long getDiscordId() {
		return discordId;
	}
}
