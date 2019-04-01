package DAO.Entities;

public class UserInfo {
	private int playcount;
	private String image;
	private String username;

	public UserInfo(int playcount, String image, String username) {
		this.playcount = playcount;
		this.image = image;
		this.username = username;
	}

	public int getPlaycount() {
		return playcount;
	}

	public void setPlaycount(int playcount) {
		this.playcount = playcount;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
