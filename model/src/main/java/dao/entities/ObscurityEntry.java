package dao.entities;

import dao.utils.LinkUtils;

public class ObscurityEntry extends LbEntry {
    public ObscurityEntry(String lastFMId, long discordId, int crowns) {
        super(lastFMId, discordId, crowns);
    }

    @Override
    public String toStringWildcard() {
        return ". [" +
                LinkUtils.cleanMarkdownCharacter(getDiscordName()) +
                "](" + WILDCARD +
                "): " + getEntryCount() +
                " obscurity points\n";
    }

}

