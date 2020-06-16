package dao.entities;

import core.commands.CommandUtil;

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
    public String toString() {
        return ". [" +
                CommandUtil.cleanMarkdownCharacter(getDiscordName()) +
                "](" + CommandUtil.getLastFmUser(this.getLastFmId()) +
                ") - " + getEntryCount() +
                " artists\n";
    }

}
