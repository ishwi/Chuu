package core.services;

import core.apis.last.ConcurrentLastFM;
import dao.ChuuService;
import dao.entities.EntityInfo;
import dao.entities.Genre;
import dao.entities.ScrobbledArtist;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

abstract class TagService<T extends EntityInfo, Y extends ScrobbledArtist> implements ChuuRunnable {
    final ChuuService dao;
    final ConcurrentLastFM lastFM;
    final Map<Genre, List<T>> genres;

    public TagService(ChuuService dao, ConcurrentLastFM lastFM, List<T> collect, String genre) {
        this(dao, lastFM, Map.of(new Genre(genre, ""), collect));
    }

    public TagService(ChuuService dao, ConcurrentLastFM lastFM, List<String> tags, T albumInfo) {
        this(dao, lastFM, tags.stream()
                .collect(Collectors.toMap(t -> new Genre(t, null), t -> Collections.singletonList(albumInfo), (t, y) -> t)));
    }


    public TagService(ChuuService dao, ConcurrentLastFM lastFM, Map<Genre, List<T>> genres) {
        this.dao = dao;
        this.lastFM = lastFM;
        this.genres = genres;
    }

    @Override
    public void execute() {
        List<T> entities = this.genres.values().stream().flatMap(Collection::stream).toList();
        Map<T, Y> validate = validate(entities);
        Set<String> bannedTags = dao.getBannedTags();
        Set<Pair<String, String>> artistBannedTags = dao.getArtistBannedTags();

        Map<Genre, List<Y>> validatedEntities = this.genres.entrySet().stream()
                .filter(x -> !bannedTags.contains(x.getKey().getGenreName().toLowerCase()))
                .filter(x -> x.getValue().stream().noneMatch(a -> artistBannedTags.contains(Pair.of(a.getArtist().toLowerCase(), x.getKey().getGenreName().toLowerCase()))))
                .collect(Collectors.toMap(Map.Entry::getKey, k -> k.getValue().stream().map(validate::get).collect(Collectors.toList())));
        insertGenres(validatedEntities);
    }

    protected abstract void insertGenres(Map<Genre, List<Y>> genres);

    protected abstract Map<T, Y> validate(List<T> collect);
}
