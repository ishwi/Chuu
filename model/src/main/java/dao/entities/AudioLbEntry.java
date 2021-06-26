package dao.entities;

import dao.utils.LinkUtils;

public class AudioLbEntry extends LbEntry<Float> {
    private final AudioStats stat;

    public AudioLbEntry(String user, long discordId, Float entryCount, AudioStats stat) {
        super(user, discordId, entryCount);
        this.stat = stat;
    }

    public AudioStats getStat() {
        return stat;
    }

    @Override
    public String toStringWildcard() {
        return ". [" +
               LinkUtils.cleanMarkdownCharacter(getDiscordName()) +
               "](" + LbEntry.WILDCARD +
               ") -  **" + stat.toValue(getEntryCount()) +
               "**\n";
    }
}

