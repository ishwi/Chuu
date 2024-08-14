package core.services.tags;

import core.apis.last.ConcurrentLastFM;
import core.services.ChuuRunnable;
import dao.ChuuService;
import dao.entities.EntityInfo;
import dao.entities.Genre;
import dao.entities.ScrobbledArtist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract class TagService<T extends EntityInfo, Y extends ScrobbledArtist> implements ChuuRunnable {
    private static final Pattern yearFilter = Pattern.compile("\\d{2,4}'?s?", Pattern.CASE_INSENSITIVE);
    final ChuuService dao;
    final ConcurrentLastFM lastFM;
    final Map<Genre, List<T>> genres;

    TagService(ChuuService dao, ConcurrentLastFM lastFM, List<T> items, String genre) {
        this(dao, lastFM, Map.of(new Genre(genre), items));
    }

    TagService(ChuuService dao, ConcurrentLastFM lastFM, List<String> tags, T albumInfo) {
        this(dao, lastFM, tags.stream()
                .collect(Collectors.toMap(t -> new Genre(t), t -> Collections.singletonList(albumInfo), (t, y) -> t)));
    }


    TagService(ChuuService dao, ConcurrentLastFM lastFM, Map<Genre, List<T>> genres) {
        this.dao = dao;
        this.lastFM = lastFM;
        this.genres = genres;
    }

    @Override
    public void execute() {
        List<T> entities = this.genres.values().stream().flatMap(Collection::stream).toList();
        Map<T, Y> validate = validate(entities);
        Set<Genre> bannedTags = dao.getBannedTags();

        Set<ArtistGenre> artistBannedTags = dao.getArtistBannedTags().stream().map(t -> new ArtistGenre(t.artist(), new Genre(t.tag()))).collect(Collectors.toSet());

        Map<Genre, List<Y>> validatedEntities = this.genres.entrySet().stream()
                .filter(x -> !yearFilter.matcher(x.getKey().getName()).find())
                .filter(x -> !bannedTags.contains(x.getKey()))
                .filter(x -> x.getValue().stream().noneMatch(a -> artistBannedTags.contains(new ArtistGenre(a.getArtist().toLowerCase(), x.getKey()))))
                .collect(Collectors.toMap(Map.Entry::getKey, k -> k.getValue().stream().map(validate::get)
                        .collect(Collectors.toCollection(ArrayList::new)), (f, s) -> {
                    f.addAll(s);
                    return f;
                }));
        insertGenres(validatedEntities);
    }

    protected abstract void insertGenres(Map<Genre, List<Y>> genres);

    protected abstract Map<T, Y> validate(List<T> toValidate);

    private record ArtistGenre(String artist, Genre genre) {

    }
}
