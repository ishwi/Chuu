package DAO.Entities;

import java.util.List;

public class ArtistAlbums {
	private final String artist;
	private List<AlbumUserPlays> albumList;

	public ArtistAlbums(String artist, List<AlbumUserPlays> albumList) {
		this.artist = artist;
		this.albumList = albumList;
	}

	public List<AlbumUserPlays> getAlbumList() {
		return albumList;
	}

	public String getArtist() {
		return artist;
	}

	public void setAlbumList(List<AlbumUserPlays> albumList) {
		this.albumList = albumList;
	}
}
