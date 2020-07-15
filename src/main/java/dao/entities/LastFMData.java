package dao.entities;

import core.commands.CommandUtil;

public class LastFMData {

    private Long discordId;
    private String name;
    private long guildID;
    private Role role;
    private final boolean privateUpdate;
    private final boolean imageNotify;
    private final WhoKnowsMode whoKnowsMode;
    private final ChartMode ChartMode;
    private final RemainingImagesMode remainingImagesMode;
    private final int defaultX;
    private final int defaultY;
    private final PrivacyMode privacyMode;
    private final boolean ratingNotify;


    public LastFMData(String name, Long discordId, long guildID, boolean privateUpdate, boolean imageNotify, WhoKnowsMode whoKnowsMode, dao.entities.ChartMode chartMode, RemainingImagesMode remainingImagesMode, int defaultX, int defaultY, PrivacyMode privacyMode, boolean ratingNotify) {
        this.discordId = discordId;
        this.name = name;
        this.guildID = guildID;
        this.privateUpdate = privateUpdate;
        this.imageNotify = imageNotify;
        this.whoKnowsMode = whoKnowsMode;
        ChartMode = chartMode;
        this.remainingImagesMode = remainingImagesMode;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
        this.privacyMode = privacyMode;
        this.ratingNotify = ratingNotify;
    }

    public LastFMData(String lastFmID, long resDiscordID, Role role, boolean privateUpdate, boolean notifyImage, WhoKnowsMode whoKnowsMode, dao.entities.ChartMode chartMode, RemainingImagesMode remainingImagesMode, int defaultX, int defaultY, PrivacyMode privacyMode, boolean ratingNotify) {
        this.name = lastFmID;
        this.discordId = resDiscordID;
        this.role = role;
        this.privateUpdate = privateUpdate;
        this.imageNotify = notifyImage;
        this.whoKnowsMode = whoKnowsMode;
        ChartMode = chartMode;
        this.remainingImagesMode = remainingImagesMode;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
        this.privacyMode = privacyMode;
        this.ratingNotify = ratingNotify;
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

    public WhoKnowsMode getWhoKnowsMode() {
        return whoKnowsMode;
    }

    public dao.entities.ChartMode getChartMode() {
        return ChartMode;
    }

    public RemainingImagesMode getRemainingImagesMode() {
        return remainingImagesMode;
    }

    public int getDefaultX() {
        return defaultX;
    }

    public int getDefaultY() {
        return defaultY;
    }

    public PrivacyMode getPrivacyMode() {
        return privacyMode;
    }

    public boolean isRatingNotify() {
        return ratingNotify;
    }


}