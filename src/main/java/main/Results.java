package main;

public class Results {
	private int countA;
	private int countB;
	private String artistID;
	private String userA;
	private String userB;
	private String url;

	public Results(int countA, int countB, String artistID, String userA, String userB, String url) {
		this.countA = countA;
		this.countB = countB;
		this.artistID = artistID;
		this.userA = userA;
		this.userB = userB;
		this.url = url;
	}

	public int getCountA() {
		return countA;
	}

	public void setCountA(int countA) {
		this.countA = countA;
	}

	public int getCountB() {
		return countB;
	}

	public void setCountB(int countB) {
		this.countB = countB;
	}

	public String getArtistID() {
		return artistID;
	}

	public void setArtistID(String artistID) {
		this.artistID = artistID;
	}

	public String getUserA() {
		return userA;
	}

	public void setUserA(String userA) {
		this.userA = userA;
	}

	public String getUserB() {
		return userB;
	}

	public void setUserB(String userB) {
		this.userB = userB;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
