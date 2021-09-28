package core.services;

import com.google.common.collect.Lists;
import dao.ChuuService;
import dao.musicbrainz.MusicBrainzService;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public record MbidFetcher(ChuuService db, MusicBrainzService mb) {

    public <W, Y> List<Y> doFetch(Supplier<List<W>> supplier, Function<List<W>, List<Y>> mapper, Comparator<Y> sort) {
        List<W> userArtists = supplier.get();
        List<List<W>> partition = Lists.partition(userArtists, 25000);
        return partition.parallelStream().map(mapper).flatMap(Collection::stream).sorted(sort).toList();
    }

    public <W, Y> Y doFetch(Supplier<List<W>> supplier, Function<List<W>, Y> mapper, Y identity, BinaryOperator<Y> reducer) {
        List<W> userArtists = supplier.get();
        List<List<W>> partition = Lists.partition(userArtists, 25000);
        return partition.parallelStream().map(mapper).reduce(identity, reducer);
    }
}
