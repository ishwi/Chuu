package dao.entities;

import dao.utils.LinkUtils;

public class UniqueSongLbEntry extends LbEntry {

    public UniqueSongLbEntry(String user, long discordId, int entryCount) {
        super(user, discordId, entryCount);
    }

    @Override
    public String toStringWildcard() {
        return ". [" +
                LinkUtils.cleanMarkdownCharacter(getDiscordName()) +
                "](" + WILDCARD +
                ") - " + getEntryCount() +
                (getEntryCount() == 1 ? " unique song" : "  unique songs\n");

    }

}
