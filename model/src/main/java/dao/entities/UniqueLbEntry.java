package dao.entities;

import dao.utils.LinkUtils;

public class UniqueLbEntry extends LbEntry {

    public UniqueLbEntry(String user, long discordId, int entryCount) {
        super(user, discordId, entryCount);
    }

    @Override
    public String toStringWildcard() {
        return ". [" +
               LinkUtils.cleanMarkdownCharacter(getDiscordName()) +
               "](" + WILDCARD +
               ") - " + getEntryCount() +
               " unique artists\n";
    }

}
