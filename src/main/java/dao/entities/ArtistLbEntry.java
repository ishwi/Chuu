package dao.entities;

import core.commands.CommandUtil;

public class ArtistLbEntry extends LbEntry {
    public ArtistLbEntry(String lastFMId, long discordId, int artistCount) {
        super(lastFMId, discordId, artistCount);
    }

    @Override
    public String toString() {
        return ". [" +
               CommandUtil.cleanMarkdownCharacter(getDiscordName()) +
               "](" + CommandUtil.getLastFmUser(this.getLastFmId()) +
               ") - " + getEntryCount() +
               " artists\n";
    }

}

