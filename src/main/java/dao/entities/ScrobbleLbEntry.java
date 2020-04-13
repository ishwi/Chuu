package dao.entities;

import core.commands.CommandUtil;

public class ScrobbleLbEntry extends LbEntry {
    public ScrobbleLbEntry(String user, long discordId, int entryCount) {
        super(user, discordId, entryCount);
    }


    @Override
    public String toString() {
        return ". [" +
               CommandUtil.cleanMarkdownCharacter(getDiscordName()) +
               "](" + CommandUtil.getLastFmUser(this.getLastFmId()) +
               "): " + getEntryCount() +
               " scrobbles\n";
    }
}
