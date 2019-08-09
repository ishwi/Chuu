package DAO.Entities;

import java.util.ArrayList;
import java.util.List;

public class FullAlbumEntity {
	private final String artist;
	private final String album;
	private final int totalPlayNumber;
	private final String url;

	private final List<Track> trackList = new ArrayList<>();

	public FullAlbumEntity(String artist, String album, int totalPlayNumber, String url) {
		this.artist = artist;
		this.album = album;
		this.totalPlayNumber = totalPlayNumber;
		this.url = url;
	}

	public void addTrack(Track track) {
		trackList.add(track);
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}

	public int getTotalPlayNumber() {
		return totalPlayNumber;
	}

	public List<Track> getTrackList() {
		return trackList;
	}
}
