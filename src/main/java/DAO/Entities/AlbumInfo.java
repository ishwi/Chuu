package DAO.Entities;

public class AlbumInfo extends EntityInfo {
	private String name;

	public AlbumInfo(String name, String artist) {
		super(null, artist);
		this.name = name;
	}

	public AlbumInfo(String mbid, String name, String artist) {
		super(mbid, artist);
		this.name = name;
	}

	public AlbumInfo(String mbid) {
		super(mbid, null);
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}
