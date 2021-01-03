package dao.entities;

import java.util.TimeZone;

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
    private final boolean privateLastfmId;
    private final boolean showBotted;
    private final TimeZone timeZone;
    private final String token;
    private String session;
    private final boolean scrobbling;

    public LastFMData(String name, Long discordId, long guildID, boolean privateUpdate, boolean imageNotify, WhoKnowsMode whoKnowsMode, dao.entities.ChartMode chartMode, RemainingImagesMode remainingImagesMode, int defaultX, int defaultY, PrivacyMode privacyMode, boolean ratingNotify, boolean privateLastfmId, boolean showBotted, TimeZone timeZone, String token, String session, boolean scrobbling) {
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
        this.privateLastfmId = privateLastfmId;
        this.showBotted = showBotted;
        this.timeZone = timeZone;
        this.token = token;
        this.session = session;
        this.scrobbling = scrobbling;
    }

    public LastFMData(String lastFmID, long resDiscordID, Role role, boolean privateUpdate, boolean notifyImage, WhoKnowsMode whoKnowsMode, dao.entities.ChartMode chartMode, RemainingImagesMode remainingImagesMode, int defaultX, int defaultY, PrivacyMode privacyMode, boolean ratingNotify, boolean privateLastfmId, boolean showBotted, TimeZone timeZone, String token, String session, boolean scrobbling) {
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
        this.privateLastfmId = privateLastfmId;
        this.showBotted = showBotted;
        this.timeZone = timeZone;
        this.token = token;
        this.session = session;
        this.scrobbling = scrobbling;
    }

    public static LastFMData ofUserWrapper(UsersWrapper usersWrapper) {
        return new LastFMData(usersWrapper.getLastFMName(), usersWrapper.getDiscordID(), usersWrapper.getRole(), false, false, WhoKnowsMode.IMAGE, dao.entities.ChartMode.IMAGE, RemainingImagesMode.IMAGE, 5, 5, PrivacyMode.NORMAL, false, false, true, usersWrapper.getTimeZone(), null, null, true);

    }

    public static LastFMData ofDefault() {
        return new LastFMData("chuu", -1L, Role.USER, false, false, WhoKnowsMode.IMAGE, dao.entities.ChartMode.IMAGE, RemainingImagesMode.IMAGE, 5, 5, PrivacyMode.NORMAL, false, false, true, TimeZone.getDefault(), null, null, true);

    }

    public static LastFMData ofUser(String user) {
        return new LastFMData(user, -1L, Role.USER, false, false, WhoKnowsMode.IMAGE, dao.entities.ChartMode.IMAGE, RemainingImagesMode.IMAGE, 5, 5, PrivacyMode.NORMAL, false, false, true, TimeZone.getDefault(), null, null, true);

    }

    public static void ofUserWrapper() {
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

    public String getEffectiveLastFmName() {
        if (isPrivateUpdate()) {
            return "chuubot";
        }
        return getName();
    }

    public boolean isPrivateLastfmId() {
        return privateLastfmId;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean isShowBotted() {
        return showBotted;
    }

    public String getToken() {
        return token;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }


    public boolean isScrobbling() {
        return scrobbling;
    }
}
