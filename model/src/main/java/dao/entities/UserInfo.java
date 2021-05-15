package dao.entities;

public class UserInfo {
    private int playCount;
    private String image;
    private String username;
    private int unixtimestamp;

    public UserInfo(int playCount, String image, String username, int unixtimestamp) {
        this.playCount = playCount;
        this.image = image;
        this.username = username;
        this.unixtimestamp = unixtimestamp;
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

    public int getUnixtimestamp() {
        return unixtimestamp;
    }

    public void setUnixtimestamp(int unixtimestamp) {
        this.unixtimestamp = unixtimestamp;
    }
}
