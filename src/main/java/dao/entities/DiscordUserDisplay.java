package dao.entities;

public class DiscordUserDisplay {
    private final String username;
    private final String urlImage;

    public DiscordUserDisplay(String username, String urlImage) {
        this.username = username;
        this.urlImage = urlImage;
    }

    public String getUsername() {
        return username;
    }

    public String getUrlImage() {
        return urlImage;
    }
}
