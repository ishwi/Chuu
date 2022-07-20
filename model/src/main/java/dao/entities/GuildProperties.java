package dao.entities;

public record GuildProperties(long guildId, Character prefix, int crown_threshold,
                              ChartMode chartMode, WhoKnowsDisplayMode whoKnowsDisplayMode,
                              OverrideMode overrideReactions, boolean allowReactions,
                              RemainingImagesMode remainingImagesMode, boolean deleteMessages,
                              boolean showWarnings, EmbedColor embedColor, boolean censorCovers,
                              OverrideColorMode overrideColorReactions, boolean setOnJoin) {

}
