package core.services;

import dao.ChuuService;
import gnu.trove.map.TLongCharMap;
import gnu.trove.map.hash.TLongCharHashMap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;

import static core.Chuu.DEFAULT_PREFIX;

public class PrefixService {
    private final TLongCharMap prefixMap;


    public PrefixService(ChuuService db) {
        Map<Long, Character> prefixes = db.getGuildPrefixes(DEFAULT_PREFIX);
        long[] ids = new long[prefixes.size()];
        char[] datas = new char[prefixes.size()];
        int i = 0;
        for (var entry : prefixes.entrySet()) {
            ids[i] = entry.getKey();
            datas[i] = entry.getValue();
            i++;
        }
        this.prefixMap = new TLongCharHashMap(ids, datas);
    }

    public void addGuildPrefix(long guildId, Character prefix) {
        if (prefix.equals(DEFAULT_PREFIX)) {
            prefixMap.remove(guildId);
        } else {
            prefixMap.put(guildId, prefix);
        }
    }

    public Character getCorrespondingPrefix(MessageReceivedEvent mes) {
        if (!mes.isFromGuild())
            return DEFAULT_PREFIX;
        long id = mes.getGuild().getIdLong();
        char character = prefixMap.get(id);
        return character == prefixMap.getNoEntryValue() ? DEFAULT_PREFIX : character;

    }

}
