package dao.entities;

public class LastFMData {

    private Long discordId;
    private String name;
    private long guildID;
    private Role role;

    public LastFMData(String name, Long discordId, long guildID) {
        this.discordId = discordId;
        this.name = name;
        this.guildID = guildID;
    }

    public LastFMData(String lastFmID, long resDiscordID, Role role) {
        this.name = lastFmID;
        this.discordId = resDiscordID;
        this.role = role;
    }


    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getDiscordId() {
        return discordId;
    }


    public void setDiscordId(Long discordId) {
        this.discordId = discordId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getGuildID() {
        return guildID;
    }

    public void setGuildID(long guildID) {
        this.guildID = guildID;
    }
}