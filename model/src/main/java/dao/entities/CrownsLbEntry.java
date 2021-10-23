package dao.entities;

import dao.utils.LinkUtils;

public class CrownsLbEntry extends LbEntryInt {

    public CrownsLbEntry(String user, long discordId, int entryCount) {
        super(user, discordId, entryCount);
    }

    @Override
    public String toStringWildcard() {
        return ". [" +
                LinkUtils.cleanMarkdownCharacter(getDiscordName()) +
                "](" + WILDCARD +
                ") - " + getEntryCount() +
                " crowns\n";
    }

}
