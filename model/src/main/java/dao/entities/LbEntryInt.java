package dao.entities;

public abstract class LbEntryInt extends LbEntry<Integer> {
    public LbEntryInt(String user, long discordId, int entryCount) {
        super(user, discordId, entryCount);
    }
}
