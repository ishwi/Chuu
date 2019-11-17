package dao.entities;

import java.util.List;

public class ArtistSummary {

	private final int userPlayCount;
	private final int listeners;
	private final int playcount;
	private final List<String> similars;
	private final List<String> tags;
	private final String summary;
	private final String artistname;

	public ArtistSummary(int userPlayCount, int listeners, int playcount, List<String> similars, List<String> tags, String summary, String artistname) {
		this.userPlayCount = userPlayCount;
		this.listeners = listeners;
		this.playcount = playcount;
		this.similars = similars;
		this.tags = tags;
		this.summary = summary;
		this.artistname = artistname;
	}

	public String getArtistname() {
		return artistname;
	}

	public int getUserPlayCount() {
		return userPlayCount;
	}

	public int getListeners() {
		return listeners;
	}

	public int getPlaycount() {
		return playcount;
	}

	public List<String> getSimilars() {
		return similars;
	}

	public List<String> getTags() {
		return tags;
	}

	public String getSummary() {
		return summary;
	}
}
