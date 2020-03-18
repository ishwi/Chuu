package dao.entities;

import core.commands.CommandUtil;

public class AlbumCrownLbEntry extends LbEntry {

    public AlbumCrownLbEntry(String user, long discordId, int entryCount) {
        super(user, discordId, entryCount);
    }

    @Override
    public String toString() {
        return ". [" +
               CommandUtil.cleanMarkdownCharacter(getDiscordName()) +
               "](" + CommandUtil.getLastFmUser(this.getLastFmId()) +
               ") - " + getEntryCount() +
               " album crowns\n";
    }

}
