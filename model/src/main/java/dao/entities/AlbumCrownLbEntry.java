package dao.entities;

import dao.utils.LinkUtils;

public class AlbumCrownLbEntry extends LbEntryInt {

    public AlbumCrownLbEntry(String user, long discordId, int entryCount) {
        super(user, discordId, entryCount);
    }

    @Override
    public String toStringWildcard() {
        return ". [" +
               LinkUtils.cleanMarkdownCharacter(getDiscordName()) +
               "](" + LbEntry.WILDCARD +
               ") - " + getEntryCount() +
               " album crowns\n";
    }

}
