package core.apis.last.queues;

import core.apis.discogs.DiscogsApi;
import core.apis.last.entities.chartentities.TrackDurationAlbumArtistChart;
import core.apis.last.entities.chartentities.TrackDurationChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.spotify.Spotify;
import core.commands.utils.CommandUtil;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.EntityInfo;
import dao.entities.ScrobbledAlbum;
import dao.entities.TrackInfo;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class TrackGroupAlbumQueue extends TrackGroupArtistQueue {
    public static final String defaultTrackImage = "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png";
    final MusicBrainzService mbiz;
    private final transient List<UrlCapsule> albumEntities;

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
        Map<T, UrlCapsule> mapped = listToJoin.stream().collect(Collectors.toMap(mappingFunction, urlCapsule -> urlCapsule, (t, t2) -> {
            if ((t instanceof TrackDurationChart) && (t2 instanceof TrackDurationChart)) {
                int seconds = ((TrackDurationChart) t).getSeconds();
                int i = ((TrackDurationChart) t2).getSeconds();
                ((TrackDurationChart) t).setSeconds(seconds + i);
            }
            t.setPlays(t.getPlays() + t2.getPlays());
            return t;
        }));
        albumInfos.forEach(y -> {
            UrlCapsule urlCapsule = mapped.get(obtainInfo.apply(y));
            manipFunction.accept(y, urlCapsule);
        });
    }

    @Override
    public List<UrlCapsule> setUp() {
        // We assume if an album has tracks with mbid then the whole album should have tracks with mbid


        //AlbumInfo -> AlbumChart
        Map<AlbumInfo, UrlCapsule> albumMap = this.albumEntities.stream().collect(Collectors.toMap(o ->
                new AlbumInfo(o.getAlbumName(), o.getArtistName()), o -> o, (a, b) -> {
            a.setPlays(a.getPlays() + b.getPlays());
            return a;
        }));

        // Album Url -> AlbumChart
        Map<String, UrlCapsule> urlMap = this.albumEntities.stream()
                .filter(x -> !x.getUrl().isBlank() && !x.getUrl().equals(defaultTrackImage))
                .collect(Collectors.toMap(UrlCapsule::getUrl, o -> o));


        //Track Url -> TrackChart
        Map<String, List<UrlCapsule>> possibleAlbumsUrl = artistMap.values()
                .stream()
                .filter(x -> !x.getUrl().isBlank() && !x.getUrl().equals(defaultTrackImage))
                .collect(Collectors.groupingBy(UrlCapsule::getUrl));

        List<UrlCapsule> mbidGrouped = new ArrayList<>();

        for (List<UrlCapsule> value : possibleAlbumsUrl.values()) {
            value.forEach(x -> x.setAlbumName(""));
            List<UrlCapsule> albumsGruoped = TrackDurationAlbumArtistChart.getGrouped(value);
            UrlCapsule urlCapsule = albumsGruoped.get(0);
            UrlCapsule urlCapsule1 = urlMap.get(urlCapsule.getUrl());
            albumsGruoped.forEach(x -> x.setAlbumName(urlCapsule1.getAlbumName()));
            mbidGrouped.addAll(albumsGruoped);
        }
        Set<UrlCapsule> tracksFoundByURL = possibleAlbumsUrl.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        artistMap.entrySet().removeIf(x -> tracksFoundByURL.contains(x.getValue()));

        Map<Boolean, List<UrlCapsule>> partitioned = new ArrayList<>(artistMap.values()).stream().collect(Collectors.partitioningBy(x -> !x.getMbid().isBlank()));
        List<UrlCapsule> haveMbid = partitioned.get(true);
        List<UrlCapsule> noMbid = partitioned.get(false);

        if (haveMbid.size() != 0) {
            convert(haveMbid);
            mbidGrouped = TrackDurationAlbumArtistChart.getGrouped(haveMbid);
        }
        cleanGrouped(noMbid, albumMap, mbidGrouped, false);
        List<UrlCapsule> mbGroupedByName;
        List<UrlCapsule> notFound = new ArrayList<>();
        if (noMbid.size() != 0) {
            joinAlbumInfos(urlCapsule -> new TrackInfo(urlCapsule.getArtistName(), null, urlCapsule.getAlbumName(), null),
                    t -> t,
                    this::wrapper,
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
                            List<UrlCapsule> artistItems = albumMap.values().stream()
                                    .filter(y -> y.getArtistName().equals(x.getArtistName()) && x.getPlays() <= y.getPlays())
                                    .sorted((o1, o2) -> Integer.compare(o2.getPlays(), o1.getPlays()))
                                    .toList();
                            int size = artistItems.size();
                            if (size == 1) {
                                handler.put(new AlbumInfo(artistItems.get(0).getAlbumName(), x.getArtistName()), x.getPlays());
                                x.setUrl(artistItems.get(0).getUrl());
                                x.setAlbumName(artistItems.get(0).getAlbumName());
                            } else if (size > 1) {
                                boolean done = false;
                                for (UrlCapsule possibleAlbumHits : artistItems) {
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
        List<UrlCapsule> items = mbidGrouped.stream().sorted(comparator())
                .takeWhile(urlCapsule -> {
                    int i = counter.getAndIncrement();
                    urlCapsule.setPos(i);
                    return i < requested;
                }).toList();
        for (UrlCapsule t : items) {
            wrapper.offer(CommandUtil.supplyLog(() ->
            {
                if (t.getUrl() == null || t.getUrl().equals(defaultTrackImage)) {
                    getUrl(t);
                }
                return t;
            }));
        }
        this.ready = true;
        this.count = items.size();
        return items;
    }

    private List<TrackInfo> wrapper(List<UrlCapsule> wrapped) {
        List<AlbumInfo> mappedAlbums = wrapped.stream().map(x -> new AlbumInfo(x.getMbid(), x.getAlbumName(), x.getArtistName())).toList();
        return mbiz.getAlbumInfoByNames(mappedAlbums);
    }

    private void convert(List<UrlCapsule> capsules) {
        Map<ScrobbledAlbum, UrlCapsule> map = capsules.stream().collect(Collectors.toMap(x -> new ScrobbledAlbum(x.getAlbumName(), x.getArtistName(), x.getUrl(), x.getMbid()), x -> x));
        mbiz.getAlbumInfoByMbid(List.copyOf(map.keySet()));
        map.keySet().forEach(x -> {
            UrlCapsule urlCapsule = map.get(x);
            assert urlCapsule != null;
            urlCapsule.setAlbumName(x.getAlbum());
            urlCapsule.setArtistName(x.getArtist());
            urlCapsule.setMbid(x.getAlbumMbid());
            urlCapsule.setPlays(x.getCount());
        });
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
