package DAO.Entities;

import java.util.List;

public class ArtistAlbums {
	private final String artist;
	private final List<AlbumInfo> albumList;

	public ArtistAlbums(String artist, List<AlbumInfo> albumList) {
		this.artist = artist;
		this.albumList = albumList;
	}

	public String getArtist() {
		return artist;
	}

	public List<AlbumInfo> getAlbumList() {
		return albumList;
	}
}
