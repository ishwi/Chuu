package core.services;

import core.apis.last.ConcurrentLastFM;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.Genre;
import dao.entities.ScrobbledAlbum;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class TagAlbumService extends TagService<AlbumInfo, ScrobbledAlbum> {
    public TagAlbumService(ChuuService dao, ConcurrentLastFM lastFM, Map<Genre, List<AlbumInfo>> genres) {
        super(dao, lastFM, genres);
    }


    public TagAlbumService(ChuuService dao, ConcurrentLastFM lastFM, List<AlbumInfo> collect, String genre) {
        super(dao, lastFM, collect, genre);
    }

    public TagAlbumService(ChuuService dao, ConcurrentLastFM lastFM, List<String> tags, AlbumInfo albumInfo) {
        super(dao, lastFM, tags, albumInfo);
    }

    @Override
    protected void insertGenres(Map<Genre, List<ScrobbledAlbum>> genres) {
        dao.insertAlbumTags(genres);
    }

    @Override
    protected Map<AlbumInfo, ScrobbledAlbum> validate(List<AlbumInfo> toValidate) {
        Map<AlbumInfo, ScrobbledAlbum> scrobbledAlbumMap = new HashMap<>();
//        Map<String, AlbumInfo> mbidIndex = toValidate.stream().collect(Collectors.toMap(EntityInfo::getMbid, x -> x));
        List<ScrobbledAlbum> a = this.dao.fillAlbumIdsByMBID(toValidate);
        // TODO duplicates
        Map<String, ScrobbledAlbum> foudnAlbumIndexMap = a.stream().collect(Collectors.toMap(ScrobbledAlbum::getAlbumMbid, x -> x, (x, y) -> x));
        Set<String> foundMbids = foudnAlbumIndexMap.keySet();
        Map<Boolean, List<AlbumInfo>> collect = toValidate.stream().collect(Collectors.partitioningBy(x -> foundMbids.contains(x.getMbid())));
        List<AlbumInfo> foundAlbums = collect.get(true);
        foundAlbums.forEach(x -> scrobbledAlbumMap.put(x, foudnAlbumIndexMap.get(x.getMbid())));
        List<AlbumInfo> notFoundAlbums = collect.get(false);
        notFoundAlbums.stream().map(x -> {
            try {
                return Pair.of(x, CommandUtil.lightAlbumValidate(dao, x.getArtist(), x.getName(), lastFM));
            } catch (LastFmException exception) {
                return null;
            }
        }).filter(Objects::nonNull).forEach(x -> scrobbledAlbumMap.put(x.getKey(), x.getValue()));
        return scrobbledAlbumMap;
    }
}
