package dao.entities;

import dao.utils.LinkUtils;

public class ScrobbleLbEntry extends LbEntry {
    public ScrobbleLbEntry(String user, long discordId, int entryCount) {
        super(user, discordId, entryCount);
    }


    @Override
    public String toStringWildcard() {
        return ". [" +
                LinkUtils.cleanMarkdownCharacter(getDiscordName()) +
                "](" + WILDCARD +
                "): " + getEntryCount() +
                " scrobbles\n";
    }
}
