package core.services;

import core.apis.last.ConcurrentLastFM;
import dao.ChuuService;
import dao.entities.EntityInfo;
import dao.entities.Genre;
import dao.entities.ScrobbledArtist;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract class TagService<T extends EntityInfo, Y extends ScrobbledArtist> implements ChuuRunnable {
    private static final Pattern yearFilter = Pattern.compile("\\d{2,4}'?s?", Pattern.CASE_INSENSITIVE);
    final ChuuService dao;
    final ConcurrentLastFM lastFM;
    final Map<Genre, List<T>> genres;

    public TagService(ChuuService dao, ConcurrentLastFM lastFM, List<T> items, String genre) {
        this(dao, lastFM, Map.of(new Genre(genre), items));
    }

    public TagService(ChuuService dao, ConcurrentLastFM lastFM, List<String> tags, T albumInfo) {
        this(dao, lastFM, tags.stream()
                .collect(Collectors.toMap(t -> new Genre(t), t -> Collections.singletonList(albumInfo), (t, y) -> t)));
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
        Set<Genre> bannedTags = dao.getBannedTags().stream().map(t -> new Genre(t)).collect(Collectors.toSet());
        Set<Pair<String, Genre>> artistBannedTags = dao.getArtistBannedTags().stream().map(t -> Pair.of(t.getLeft(), new Genre(t.getRight()))).collect(Collectors.toSet());

        Map<Genre, List<Y>> validatedEntities = this.genres.entrySet().stream()
                .filter(x -> !yearFilter.matcher(x.getKey().getName()).find())
                .filter(x -> !bannedTags.contains(x.getKey()))
                .filter(x -> x.getValue().stream().noneMatch(a -> artistBannedTags.contains(Pair.of(a.getArtist().toLowerCase(), x.getKey()))))
                .collect(Collectors.toMap(Map.Entry::getKey, k -> k.getValue().stream().map(validate::get)
                        .collect(Collectors.toCollection(ArrayList::new)), (f, s) -> {
                    f.addAll(s);
                    return f;
                }));
        insertGenres(validatedEntities);
    }

    protected abstract void insertGenres(Map<Genre, List<Y>> genres);

    protected abstract Map<T, Y> validate(List<T> toValidate);
}
