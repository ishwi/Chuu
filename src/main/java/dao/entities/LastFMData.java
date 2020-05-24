package dao.entities;

public class LastFMData {

    private Long discordId;
    private String name;
    private long guildID;
    private Role role;
    private final boolean privateUpdate;
    private final boolean imageNotify;
    private final boolean additionalEmbedChart;

    public LastFMData(String name, Long discordId, long guildID, boolean privateUpdate, boolean imageNotify, boolean additionalEmbedChart) {
        this.discordId = discordId;
        this.name = name;
        this.guildID = guildID;
        this.privateUpdate = privateUpdate;
        this.imageNotify = imageNotify;
        this.additionalEmbedChart = additionalEmbedChart;
    }

    public LastFMData(String lastFmID, long resDiscordID, Role role, boolean privateUpdate, boolean notifyImage, boolean additionalEmbedChart) {
        this.name = lastFmID;
        this.discordId = resDiscordID;
        this.role = role;
        this.privateUpdate = privateUpdate;
        this.imageNotify = notifyImage;
        this.additionalEmbedChart = additionalEmbedChart;
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

    public boolean isPrivateUpdate() {
        return privateUpdate;
    }

    public boolean isImageNotify() {
        return imageNotify;
    }

    public boolean isAdditionalEmbedChart() {
        return additionalEmbedChart;
    }
}