package DAO.Entities;

public class LastFMData {

	private Long showID;
	private String name;


	public LastFMData(String name, Long showID) {
		this.showID = showID;
		this.name = name;
	}


	public Long getShowID() {
		return showID;
	}


	public void setShowID(Long showID) {
		this.showID = showID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}