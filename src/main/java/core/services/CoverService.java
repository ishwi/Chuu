package core.services;

import core.commands.Context;
import core.commands.utils.CommandUtil;
import dao.ChuuService;
import dao.entities.Album;
import dao.entities.CoverItem;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CoverService {


    private final Map<CoverItem, List<String>> bannedCovers;
    private final Map<Long, List<String>> bannedCoversById;
    private final Set<Long> guildsAcceptingAll;
    private final ChuuService db;

    public CoverService(ChuuService db) {
        bannedCovers = db.getBannedCovers();
        guildsAcceptingAll = db.getGuildsAcceptingCovers();
        bannedCoversById = new HashMap<>();
        bannedCovers.forEach((key, value) -> bannedCoversById.put(key.albumId(), value));
        this.db = db;
    }

    public void addCover(CoverItem coverItem, String cover) {
        db.insertBannedCover(coverItem.albumId(), cover);
        bannedCovers.computeIfAbsent(coverItem, k -> new ArrayList<>()).add(cover);
        bannedCoversById.computeIfAbsent(coverItem.albumId(), k -> new ArrayList<>()).add(cover);
    }


    public String getCover(long albumId, String replacement, Context e) {
        return obtain(replacement, e, () -> bannedCoversById.getOrDefault(albumId, Collections.emptyList()));
    }

    public String getCover(Album album, Context e) {
        return obtain(album.url(), e, () -> bannedCoversById.getOrDefault(album.id(), Collections.emptyList()));
    }

    public List<String> getCovers(long albumId) {
        return bannedCoversById.getOrDefault(albumId, Collections.emptyList());
    }

    public String getCover(CoverItem coverItem, String replacement, Context e) {
        return obtain(replacement, e, () -> bannedCovers.getOrDefault(coverItem, Collections.emptyList()));
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
        return bannedCovers.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, t -> t.getValue().size()));
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
