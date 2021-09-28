package dao.entities;

import dao.utils.LinkUtils;

public class UniqueAlbumLbEntry extends LbEntryInt {

    public UniqueAlbumLbEntry(String user, long discordId, int entryCount) {
        super(user, discordId, entryCount);
    }

    @Override
    public String toStringWildcard() {
        return ". [" +
               LinkUtils.cleanMarkdownCharacter(getDiscordName()) +
               "](" + WILDCARD +
               ") - " + getEntryCount() +
               (getEntryCount() == 1 ? " unique album" : "  unique albums\n");

    }

}
