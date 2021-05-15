package dao.entities;

import dao.utils.LinkUtils;

public class TrackCrownLbEntry extends LbEntry {

    public TrackCrownLbEntry(String user, long discordId, int entryCount) {
        super(user, discordId, entryCount);
    }

    @Override
    public String toStringWildcard() {
        return ". [" +
               LinkUtils.cleanMarkdownCharacter(getDiscordName()) +
               "](" + LbEntry.WILDCARD +
               ") - " + getEntryCount() +
               " track crowns\n";
    }

}
