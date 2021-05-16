package core.services;

import core.commands.Context;
import core.commands.utils.CommandUtil;
import dao.ChuuService;
import dao.entities.Album;
import dao.entities.CoverItem;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CoverService {


    private final ListValuedMap<CoverItem, String> bannedCovers;
    private final ListValuedMap<Long, String> bannedCoversById;
    private final Set<Long> guildsAcceptingAll;
    private final ChuuService db;

    public CoverService(ChuuService db) {
        bannedCovers = db.getBannedCovers();
        guildsAcceptingAll = db.getGuildsAcceptingCovers();
        bannedCoversById = new ArrayListValuedHashMap<>();
        bannedCovers.asMap().forEach((key, value) -> bannedCoversById.putAll(key.albumId(), value));
        this.db = db;
    }

    public void addCover(CoverItem coverItem, String cover) {
        db.insertBannedCover(coverItem.albumId(), cover);
        bannedCovers.put(coverItem, cover);
        bannedCoversById.put(coverItem.albumId(), cover);
    }


    public void removeCover(long albumId, String cover) {
        db.insertBannedCover(albumId, cover);
        bannedCovers.removeMapping(albumId, cover);
    }

    public String getCover(long albumId, String replacement, Context e) {
        return obtain(replacement, e, () -> bannedCoversById.get(albumId));
    }

    public String getCover(Album album, Context e) {
        return obtain(album.url(), e, () -> bannedCoversById.get(album.id()));
    }

    public List<String> getCovers(long albumId) {
        return bannedCoversById.get(albumId);
    }

    public String getCover(CoverItem coverItem, String replacement, Context e) {
        return obtain(replacement, e, () -> bannedCovers.get(coverItem));
    }

    private String obtain(String replacement, Context e, Supplier<List<String>> altCovers) {
        if (dontCensor(e)) {
            return replacement;
        }
        List<String> altCover = altCovers.get();
        if (altCover.isEmpty()) {
            return replacement;
        }
        String value = altCover.get(CommandUtil.rand.nextInt(altCover.size()));
        if (value != null) return value;
        return replacement;
    }

    public String getCover(String artist, String album, String replacement, Context e) {
        return getCover(new CoverItem(album, artist), replacement, e);
    }


    public Map<CoverItem, Integer> getCounts() {
        return bannedCovers.asMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, t -> t.getValue().size()));
    }

    private boolean dontCensor(Context e) {
        return e.isFromGuild() && (guildsAcceptingAll.contains(e.getGuild().getIdLong()) || (e.getChannel() != null && e.getChannel() instanceof TextChannel textChannel && textChannel.isNSFW()));
    }

    public void removeServer(long guildId) {
        this.guildsAcceptingAll.remove(guildId);
    }

    public void addServer(long guildId) {
        this.guildsAcceptingAll.add(guildId);
    }


}
