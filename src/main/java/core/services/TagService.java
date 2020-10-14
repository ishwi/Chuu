package core.services;

import core.apis.last.ConcurrentLastFM;
import dao.ChuuService;
import dao.entities.EntityInfo;
import dao.entities.Genre;
import dao.entities.ScrobbledArtist;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

abstract class TagService<T extends EntityInfo, Y extends ScrobbledArtist> implements ChuuRunnable {
    final ChuuService dao;
    final ConcurrentLastFM lastFM;
    Map<Genre, List<T>> genres;

    public TagService(ChuuService dao, ConcurrentLastFM lastFM, List<T> collect, String genre) {
        this(dao, lastFM, Map.of(new Genre(genre, ""), collect));
    }

    public TagService(ChuuService dao, ConcurrentLastFM lastFM, Map<Genre, List<T>> genres) {
        this.dao = dao;
        this.lastFM = lastFM;
        this.genres = genres;
    }

    @Override
    public void execute() {
        List<T> entities = this.genres.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        Map<T, Y> validate = validate(entities);
        Set<String> bannedTags = dao.getBannedTags();
        Map<Genre, List<Y>> validatedEntities = this.genres.entrySet().stream().filter(x -> !bannedTags.contains(x.getKey().getGenreName())).collect(Collectors.toMap(Map.Entry::getKey, k -> k.getValue().stream().map(validate::get).collect(Collectors.toList())));
        insertGenres(validatedEntities);
    }

    protected abstract void insertGenres(Map<Genre, List<Y>> genres);

    protected abstract Map<T, Y> validate(List<T> collect);
}
