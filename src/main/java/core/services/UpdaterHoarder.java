package core.services;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.*;

import java.util.*;
import java.util.stream.Collectors;

import static core.scheduledtasks.UpdaterThread.groupAlbumsToArtist;

public class UpdaterHoarder {

    private final ChuuService service;
    private final DiscogsApi discogsApi;
    private final Spotify spotify;
    private final ConcurrentLastFM lastFM;
    private final UsersWrapper user;
    private final Map<String, Long> dbIdMap = new HashMap<>();

    public UpdaterHoarder(UsersWrapper user, ChuuService service, ConcurrentLastFM lastFM) {
        this.user = user;
        this.lastFM = lastFM;
        spotify = SpotifySingleton.getInstance();
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.service = service;
    }

    public void updateUser() throws LastFmException {
        List<TrackWithArtistId> trackWithArtistIds;

        String lastFMName = user.getLastFMName();
        trackWithArtistIds = lastFM.getWeeklyBillboard(lastFMName,
                user.getTimestamp()
                , Integer.MAX_VALUE);
        updateList(trackWithArtistIds);
    }

    public void updateList(List<TrackWithArtistId> trackWithArtistIds) {
        if (trackWithArtistIds.isEmpty()) {
            return;
        }
        doArtistValidation(trackWithArtistIds);
        int max = trackWithArtistIds.stream().mapToInt(TrackWithArtistId::getUtc).max().orElse(user.getTimestamp()) + 1;
        Map<ScrobbledAlbum, Long> a = trackWithArtistIds.stream()
                .collect(Collectors.groupingBy(nowPlayingArtist -> {
                    ScrobbledAlbum scrobbledAlbum = new ScrobbledAlbum(nowPlayingArtist.getAlbum(), nowPlayingArtist.getArtist(), null, null);
                    scrobbledAlbum.setArtistId(nowPlayingArtist.getArtistId());
                    scrobbledAlbum.setArtistMbid(nowPlayingArtist.getArtistMbid());
                    scrobbledAlbum.setAlbumMbid(nowPlayingArtist.getAlbumMbid());
                    return scrobbledAlbum;
                }, Collectors.counting()));
        TimestampWrapper<ArrayList<ScrobbledAlbum>> albumDataList = new TimestampWrapper<>(
                a.entrySet().stream().map(
                        entry -> {
                            ScrobbledAlbum artist = entry.getKey();
                            artist.setCount(Math.toIntExact(entry.getValue()));
                            return artist;
                        })
                        .collect(Collectors.toCollection(ArrayList::new)), max);
        // Correction with current last fm implementation should return the same name so
        // no correction gives
        List<ScrobbledAlbum> albumData = albumDataList.getWrapped();
        List<ScrobbledArtist> artistData = groupAlbumsToArtist(albumData);
        service.incrementalUpdate(new TimestampWrapper<>(artistData, albumDataList.getTimestamp()), user.getLastFMName(), albumData, trackWithArtistIds);
    }


    private void doArtistValidation(List<TrackWithArtistId> toValidate) {
        List<TrackWithArtistId> newToValidate = toValidate.stream().peek(x -> {
            Long aLong1 = dbIdMap.get(x.getArtist());
            if (aLong1 != null)
                x.setArtistId(aLong1);
        }).filter(x -> x.getArtistId() == -1L || x.getArtistId() == 0L).collect(Collectors.toList());
        Set<String> tobeRemoved = new HashSet<>();
        List<ScrobbledArtist> artists = newToValidate.stream().map(Track::getArtist).distinct().map(x -> new ScrobbledArtist(x, 0, null)).collect(Collectors.toList());
        service.filldArtistIds(artists);
        Map<Boolean, List<ScrobbledArtist>> mappedByExistingId = artists.stream().collect(Collectors.partitioningBy(x -> x.getArtistId() != -1L && x.getArtistId() != 0));
        List<ScrobbledArtist> foundArtists = mappedByExistingId.get(true);
        Map<String, String> changedUserNames = new HashMap<>();
        mappedByExistingId.get(false).stream().map(x -> {
            try {
                String artist = x.getArtist();
                CommandUtil.validate(service, x, lastFM, discogsApi, spotify);
                String newArtist = x.getArtist();
                if (!Objects.equals(artist, newArtist)) {
                    changedUserNames.put(artist, newArtist);
                }
                return x;
            } catch (LastFmEntityNotFoundException exception) {
                service.upsertArtistSad(x);
                return x;
            } catch (LastFmException lastFmException) {
                tobeRemoved.add(x.getArtist());
                return null;
            }
        }).filter(Objects::nonNull).forEach(foundArtists::add);
        Map<String, Long> mapId = foundArtists.stream().collect(Collectors.toMap(ScrobbledArtist::getArtist, ScrobbledArtist::getArtistId, (x, y) -> x));

        for (Iterator<TrackWithArtistId> iterator = toValidate.iterator(); iterator.hasNext(); ) {

            TrackWithArtistId x = iterator.next();
            if (x.getArtistId() > 0) {
                continue;
            }
            Long aLong = mapId.get(x.getArtist());
            if (tobeRemoved.contains(x.getArtist())) {
                iterator.remove();
                continue;
            }
            if (aLong == null) {
                String s = changedUserNames.get(x.getArtist());
                if (s != null) {
                    aLong = mapId.get(s);
                }
                if (aLong == null) {
                    aLong = -1L;
                }
            }
            x.setArtistId(aLong);
            if (x.getArtistId() == -1L) {
                iterator.remove();
            }
            this.dbIdMap.put(x.getArtist(), x.getArtistId());
        }
    }

}
