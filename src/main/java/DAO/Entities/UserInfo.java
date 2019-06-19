package DAO.Entities;

public class UserInfo {
	private int playCount;
	private String image;
	private String username;

	public UserInfo(int playCount, String image, String username) {
		this.playCount = playCount;
		this.image = image;
		this.username = username;
	}

	public int getPlayCount() {
		return playCount;
	}

	public void setPlayCount(int playCount) {
		this.playCount = playCount;
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
