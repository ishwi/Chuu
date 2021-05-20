package core.services;

import core.Chuu;
import core.commands.Context;
import dao.ChuuService;

import java.util.Map;

import static core.Chuu.DEFAULT_PREFIX;

public class PrefixService {
    private final Map<Long, Character> prefixMap;


    public PrefixService(ChuuService db) {
        this.prefixMap = (db.getGuildPrefixes(Chuu.DEFAULT_PREFIX));
    }

    public void addGuildPrefix(long guildId, Character prefix) {
        if (prefix.equals(DEFAULT_PREFIX)) {
            prefixMap.remove(guildId);
        } else {
            Character replace = prefixMap.replace(guildId, prefix);
            if (replace == null) {
                prefixMap.put(guildId, prefix);
            }
        }
    }

    public Character getCorrespondingPrefix(Context e) {
        if (!e.isFromGuild())
            return DEFAULT_PREFIX;
        long id = e.getGuild().getIdLong();
        Character character = prefixMap.get(id);
        return character == null ? DEFAULT_PREFIX : character;

    }

    public Map<Long, Character> getPrefixMap() {
        return prefixMap;
    }
}
