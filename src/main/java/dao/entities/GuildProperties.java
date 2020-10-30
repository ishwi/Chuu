package dao.entities;

public class GuildProperties {
    private final long guildId;
    private final Character prefix;
    private final int crown_threshold;
    private final ChartMode chartMode;
    private final WhoKnowsMode whoKnowsMode;
    private final RemainingImagesMode remainingImagesMode;
    private final boolean deleteMessages;
    private final boolean showWarnings;


    public GuildProperties(long guildId, Character prefix, int crown_threshold, ChartMode chartMode, WhoKnowsMode whoKnowsMode, RemainingImagesMode remainingImagesMode, boolean deleteMessages, boolean showWarnings) {
        this.guildId = guildId;
        this.prefix = prefix;
        this.crown_threshold = crown_threshold;
        this.chartMode = chartMode;
        this.whoKnowsMode = whoKnowsMode;
        this.remainingImagesMode = remainingImagesMode;
        this.deleteMessages = deleteMessages;
        this.showWarnings = showWarnings;
    }

    public long getGuildId() {
        return guildId;
    }

    public Character getPrefix() {
        return prefix;
    }

    public int getCrown_threshold() {
        return crown_threshold;
    }

    public ChartMode getChartMode() {
        return chartMode;
    }

    public WhoKnowsMode getWhoKnowsMode() {
        return whoKnowsMode;
    }

    public RemainingImagesMode getRemainingImagesMode() {
        return remainingImagesMode;
    }

    public boolean isDeleteMessages() {
        return deleteMessages;
    }

    public boolean isShowWarnings() {
        return showWarnings;
    }
}
