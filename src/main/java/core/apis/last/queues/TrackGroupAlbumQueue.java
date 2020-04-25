package core.apis.last.queues;

import core.apis.discogs.DiscogsApi;
import core.apis.last.chartentities.TrackDurationAlbumArtistChart;
import core.apis.last.chartentities.TrackDurationChart;
import core.apis.spotify.Spotify;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.EntityInfo;
import dao.entities.TrackInfo;
import dao.entities.UrlCapsule;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class TrackGroupAlbumQueue extends TrackGroupArtistQueue {
    private final transient List<UrlCapsule> albumEntities;
    private final String defaultTrackImage = "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png";
    MusicBrainzService mbiz;

    public TrackGroupAlbumQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify, int requested, List<UrlCapsule> albumEntities) {
        super(dao, discogsApi, spotify, requested);
        this.albumEntities = albumEntities;
        mbiz = MusicBrainzServiceSingleton.getInstance();
    }

    @Override
    public Function<UrlCapsule, String> mappingFunction() {
        return urlCapsule -> urlCapsule.getArtistName() + " - " + urlCapsule.getAlbumName();
    }

    private <T extends EntityInfo> void joinAlbumInfos(Function<UrlCapsule, T> mappingFunction, UnaryOperator<T> obtainInfo, Function<List<UrlCapsule>, List<T>> function, List<UrlCapsule> listToJoin, BiConsumer<T, UrlCapsule> manipFunction) {
        List<T> albumInfos = function.apply(listToJoin);
        Map<T, UrlCapsule> collect = listToJoin.stream().collect(Collectors.toMap(mappingFunction, urlCapsule -> urlCapsule, (t, t2) -> {
            if ((t instanceof TrackDurationChart) && (t2 instanceof TrackDurationChart)) {
                int seconds = ((TrackDurationChart) t).getSeconds();
                int i = ((TrackDurationChart) t2).getSeconds();
                ((TrackDurationChart) t).setSeconds(seconds + i);
            }
            t.setPlays(t.getPlays() + t2.getPlays());
            return t;
        }));
        albumInfos.forEach(y -> {
            UrlCapsule urlCapsule = collect.get(obtainInfo.apply(y));
            manipFunction.accept(y, urlCapsule);
        });
    }

    @Override
    public List<UrlCapsule> setUp() {
        // We assume if an album has tracks with mbid then the whole album should have tracks with mbid
        Map<AlbumInfo, UrlCapsule> albumMap = this.albumEntities.stream().collect(Collectors.toMap(o ->
                new AlbumInfo(o.getAlbumName(), o.getArtistName()), o -> o));
        Map<String, UrlCapsule> urlMap = this.albumEntities.stream()
                .filter(x -> !x.getUrl().isBlank() && !x.getUrl().equals(defaultTrackImage))
                .collect(Collectors.toMap(UrlCapsule::getUrl, o -> o));
        Map<String, List<UrlCapsule>> collect1 = artistMap.values()
                .stream()
                .filter(x -> !x.getUrl().isBlank() && !x.getUrl().equals(defaultTrackImage))
                .collect(Collectors.groupingBy(UrlCapsule::getUrl));

        List<UrlCapsule> mbidGrouped = new ArrayList<>();

        for (List<UrlCapsule> value : collect1.values()) {
            value.forEach(x -> x.setAlbumName(""));
            List<UrlCapsule> albumsGruoped = TrackDurationAlbumArtistChart.getGrouped(value);
            UrlCapsule urlCapsule = albumsGruoped.get(0);
            UrlCapsule urlCapsule1 = urlMap.get(urlCapsule.getUrl());
            albumsGruoped.forEach(x -> {
                x.setAlbumName(urlCapsule1.getAlbumName());
            });

            mbidGrouped.addAll(albumsGruoped);
        }
        Set<UrlCapsule> tracksFoundByURL = collect1.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        artistMap.entrySet().removeIf(x -> tracksFoundByURL.contains(x.getValue()));

        Map<Boolean, List<UrlCapsule>> partitioned = new ArrayList<>(artistMap.values()).stream().collect(Collectors.partitioningBy(x -> !x.getMbid().isBlank()));
        List<UrlCapsule> haveMbid = partitioned.get(true);
        List<UrlCapsule> noMbid = partitioned.get(false);

        if (haveMbid.size() != 0) {
            mbiz.getAlbumInfoByMbid(haveMbid);
            mbidGrouped = TrackDurationAlbumArtistChart.getGrouped(haveMbid);
        }
        cleanGrouped(noMbid, albumMap, mbidGrouped, false);
        List<UrlCapsule> mbGroupedByName;
        List<UrlCapsule> notFound = new ArrayList<>();
        if (noMbid.size() != 0) {
            joinAlbumInfos(urlCapsule -> new TrackInfo(urlCapsule.getArtistName(), null, urlCapsule.getAlbumName(), null),
                    t -> t,
                    mbiz::getAlbumInfoByNames,
                    noMbid, (trackInfo, urlCapsule) -> {
                        urlCapsule.setAlbumName(trackInfo.getAlbum());
                        urlCapsule.setMbid(trackInfo.getAlbumMid());
                    });
            mbGroupedByName = TrackDurationAlbumArtistChart.getGrouped(noMbid);
        } else {
            mbGroupedByName = new ArrayList<>();
        }
        cleanGrouped(notFound, albumMap, mbGroupedByName, true);
        mbidGrouped.addAll(mbGroupedByName);

        Map<AlbumInfo, Integer> handler = new HashMap<>();
        noMbid.stream().sorted(Comparator.comparing(UrlCapsule::getArtistName).thenComparing(Comparator.comparing(UrlCapsule::getAlbumName).thenComparing(Comparator.comparing(UrlCapsule::getPlays).reversed())))
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
        List<UrlCapsule> grouped = TrackDurationAlbumArtistChart.getGrouped(noMbid);
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
            if (t.getUrl() == null || t.getUrl().equals(defaultTrackImage)) {
                getUrl(t);
            }
            return t;
        })));
        this.ready = true;
        this.count = collect.size();
        return collect;
    }

    private void cleanGrouped
            (List<UrlCapsule> notFound, Map<AlbumInfo, UrlCapsule> albumMap, List<UrlCapsule> groupedList,
             boolean doMbid) {
        groupedList.removeIf(x -> {
            AlbumInfo albumInfo = new AlbumInfo(x.getAlbumName(), x.getArtistName());
            if (doMbid && !x.getMbid().isBlank()) {
                albumInfo.setMbid(x.getMbid());
            }
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
