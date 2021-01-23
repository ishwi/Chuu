package dao.entities;

public record GuildProperties(long guildId, Character prefix, int crown_threshold,
                              ChartMode chartMode, WhoKnowsMode whoKnowsMode,
                              boolean overrideReactions, boolean allowReactions,
                              RemainingImagesMode remainingImagesMode, boolean deleteMessages,
                              boolean showWarnings) {

}
