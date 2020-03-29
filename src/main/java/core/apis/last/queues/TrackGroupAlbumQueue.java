package core.apis.last.queues;

import core.apis.discogs.DiscogsApi;
import core.apis.last.chartentities.TrackDurationAlbumArtistChart;
import core.apis.spotify.Spotify;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.EntityInfo;
import dao.entities.TrackInfo;
import dao.entities.UrlCapsule;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TrackGroupAlbumQueue extends TrackGroupArtistQueue {
    private final List<UrlCapsule> albumEntities;
    MusicBrainzService mbiz;

    public TrackGroupAlbumQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify, int requested, List<UrlCapsule> albumEntities) {
        super(dao, discogsApi, spotify, requested);
        this.albumEntities = albumEntities;
        mbiz = MusicBrainzServiceSingleton.getInstance();
    }

    @Override
    public Function<UrlCapsule, String> mappingFunction() {
        return urlCapsule -> urlCapsule.getArtistName() + urlCapsule.getAlbumName();
    }

    private <T extends EntityInfo> void joinAlbumInfos(Function<UrlCapsule, T> mappingFunction, Function<T, T> obtainInfo, Function<List<UrlCapsule>, List<T>> function, List<UrlCapsule> listToJoin, BiConsumer<T, UrlCapsule> manipFunction) {
        List<T> albumInfos = function.apply(listToJoin);
        Map<T, UrlCapsule> collect = listToJoin.stream().collect(Collectors.toMap(mappingFunction, urlCapsule -> urlCapsule));
        albumInfos.forEach(y -> {
            UrlCapsule urlCapsule = collect.get(obtainInfo.apply(y));
            manipFunction.accept(y, urlCapsule);
        });
    }

    @Override
    public List<UrlCapsule> setUp() {
        Map<Boolean, List<UrlCapsule>> partitioned = new ArrayList<>(artistMap.values()).stream().collect(Collectors.partitioningBy(x -> !x.getMbid().isBlank()));
        List<UrlCapsule> haveMbid = partitioned.get(true);
        List<UrlCapsule> notFound = partitioned.get(false);

        List<UrlCapsule> mbidGrouped;
        if (haveMbid.size() != 0) {
            joinAlbumInfos(urlCapsule -> {
                        AlbumInfo albumInfo = new AlbumInfo(urlCapsule.getAlbumName(), urlCapsule.getArtistName());
                        albumInfo.setMbid(urlCapsule.getMbid());
                        return albumInfo;
                    },
                    albumInfo -> albumInfo,
                    mbiz::getAlbumInfoByMbid,
                    haveMbid, (albumInfo, urlCapsule) -> urlCapsule.setAlbumName(albumInfo.getName()));
            mbidGrouped = TrackDurationAlbumArtistChart.getGrouped(haveMbid);
        } else {
            mbidGrouped = new ArrayList<>();
        }
        // We assume if an album has tracks with mbid then the whole album should have tracks with mbid
        Map<AlbumInfo, UrlCapsule> albumMap = this.albumEntities.stream().collect(Collectors.toMap(o -> new AlbumInfo(o.getAlbumName(), o.getArtistName()), o -> o));
        cleanGrouped(notFound, albumMap, mbidGrouped);
        List<UrlCapsule> mbGroupedByName;

        if (notFound.size() != 0) {
            joinAlbumInfos(urlCapsule -> new TrackInfo(urlCapsule.getArtistName(), null, urlCapsule.getAlbumName()),
                    t -> t,
                    mbiz::getAlbumInfoByNames,
                    notFound, (trackInfo, urlCapsule) ->
                            urlCapsule.setAlbumName(trackInfo.getAlbum()));
            mbGroupedByName = TrackDurationAlbumArtistChart.getGrouped(notFound);
        } else {
            mbGroupedByName = new ArrayList<>();
        }
        cleanGrouped(notFound, albumMap, mbGroupedByName);
        mbidGrouped.addAll(mbGroupedByName);

        Map<AlbumInfo, Integer> handler = new HashMap<>();
        notFound.stream().sorted(Comparator.comparing(UrlCapsule::getArtistName).thenComparing(Comparator.comparing(UrlCapsule::getAlbumName).thenComparing(Comparator.comparing(UrlCapsule::getPlays).reversed())))
                .forEachOrdered(
                        x -> {
                            List<UrlCapsule> collect = albumMap.values().stream()
                                    .filter(y -> y.getArtistName().equals(x.getArtistName()) && x.getPlays() <= y.getPlays())
                                    .sorted((o1, o2) -> Integer.compare(o2.getPlays(), o1.getPlays()))
                                    .collect(Collectors.toList());
                            int size = collect.size();
                            if (size == 1) {
                                handler.put(new AlbumInfo(collect.get(0).getAlbumName(), x.getArtistName()), x.getPlays());
                                x.setUrl(collect.get(0).getUrl());
                                x.setAlbumName(collect.get(0).getAlbumName());
                            } else if (size > 1) {
                                boolean done = false;
                                for (UrlCapsule possibleAlbumHits : collect) {
                                    AlbumInfo albumInfo = new AlbumInfo(possibleAlbumHits.getAlbumName(), x.getArtistName());
                                    Integer integer = handler.get(albumInfo);
                                    if (integer == null) {
                                        handler.put(albumInfo, x.getPlays());
                                        x.setAlbumName(possibleAlbumHits.getAlbumName());
                                        done = true;
                                        break;
                                    } else {
                                        if (integer + x.getPlays() <= possibleAlbumHits.getPlays()) {
                                            handler.merge(albumInfo, x.getPlays(), Integer::sum);
                                            x.setAlbumName(possibleAlbumHits.getAlbumName());
                                            done = true;
                                            break;
                                        }
                                    }
                                }
                                if (!done) {
                                    x.setAlbumName("Unknown Album");
                                }
                            }
                        }
                );
        List<UrlCapsule> grouped = TrackDurationAlbumArtistChart.getGrouped(notFound);
        AtomicInteger counter = new AtomicInteger(0);
        mbidGrouped.addAll(grouped);
        mbidGrouped = TrackDurationAlbumArtistChart.getGrouped(mbidGrouped);
        List<UrlCapsule> collect = mbidGrouped.stream().sorted(comparator())
                .takeWhile(urlCapsule -> {
                    int i = counter.getAndIncrement();
                    urlCapsule.setPos(i);
                    return i < requested;
                }).collect(Collectors.toList());
        collect.forEach(t -> wrapper.offer(CompletableFuture.supplyAsync(() ->
        {
            if (t.getUrl() == null) {
                getUrl(t);
            }
            return t;
        })));
        return collect;
    }

    private void cleanGrouped(List<UrlCapsule> notFound, Map<AlbumInfo, UrlCapsule> albumMap, List<UrlCapsule> groupedList) {
        groupedList.removeIf(x -> {
            AlbumInfo albumInfo = new AlbumInfo(x.getAlbumName(), x.getArtistName());
            albumInfo.setMbid(x.getMbid());
            UrlCapsule remove = albumMap.remove(albumInfo);
            if (remove == null) {
                notFound.add(x);
                return true;
            } else {
                x.setUrl(remove.getUrl());
                return false;
            }
        });
    }
}
