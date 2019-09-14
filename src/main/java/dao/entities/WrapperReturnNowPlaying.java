package dao.entities;

import java.util.List;

public class WrapperReturnNowPlaying {
	private List<ReturnNowPlaying> returnNowPlayings;
	private int rows;
	private String url;
	private String artist;

	public WrapperReturnNowPlaying(List<ReturnNowPlaying> returnNowPlayings, int rows, String url, String artist) {
		this.returnNowPlayings = returnNowPlayings;
		this.rows = rows;
		this.url = url;
		this.artist = artist;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public List<ReturnNowPlaying> getReturnNowPlayings() {
		return returnNowPlayings;
	}

	public void setReturnNowPlayings(List<ReturnNowPlaying> returnNowPlayings) {
		this.returnNowPlayings = returnNowPlayings;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}


}
