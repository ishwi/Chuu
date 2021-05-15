package dao.entities;

import dao.utils.LinkUtils;

public class ArtistLbEntry extends LbEntry {
    public ArtistLbEntry(String lastFMId, long discordId, int artistCount) {
        super(lastFMId, discordId, artistCount);
    }

    @Override
    public String toStringWildcard() {
        return ". [" +
               LinkUtils.cleanMarkdownCharacter(getDiscordName()) +
               "](" + WILDCARD +
               ") - " + getEntryCount() +
               (getEntryCount() == 1 ? " artist" : " artists\n");
    }

}

