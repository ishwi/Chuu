package dao.entities;

import dao.utils.LinkUtils;

public class ArtistLbGlobalEntry extends ArtistLbEntry {
    private final PrivacyMode privacyMode;

    public ArtistLbGlobalEntry(String lastFMId, long discordId, int artistCount, PrivacyMode privacyMode) {
        super(lastFMId, discordId, artistCount);
        this.privacyMode = privacyMode;
    }

    public PrivacyMode getPrivacyMode() {
        return privacyMode;
    }

    @Override
    public String toStringWildcard() {
        return ". [" +
                LinkUtils.cleanMarkdownCharacter(getDiscordName()) +
                "](" + WILDCARD +
                ") - " + getEntryCount() +
                " artists\n";
    }

}
