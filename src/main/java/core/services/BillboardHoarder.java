package core.services;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.CommandUtil;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.*;

import java.sql.Date;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BillboardHoarder {

    private final static ConcurrentSkipListSet<Long> usersBeingProcessed = new ConcurrentSkipListSet<>();
    private final List<UsersWrapper> users;
    private final ChuuService service;
    private final DiscogsApi discogsApi;
    private final Spotify spotify;
    private final Week week;
    private final ConcurrentLastFM lastFM;
    private final int weekId;
    private final Map<String, Long> dbIdMap = new HashMap<>();
    private final java.util.function.Function<TimeZone, Function<LocalDate, ZoneOffset>> phaserBuilder = (t) -> (LocalDate l) -> {
        try {
            return ZoneOffset.ofTotalSeconds(t.getOffset(l.getEra().getValue(), l.getYear(), l.getMonth().getValue(), l.getDayOfMonth(), l.getDayOfWeek().getValue(), 0) / 1000);
        } catch (DateTimeException ex) {
            System.out.println("asodjua");
        }
        return null;
    };


    public BillboardHoarder(List<UsersWrapper> users, ChuuService service, Week week, ConcurrentLastFM lastFM) {
        this.users = users;
        this.week = week;
        this.weekId = this.week.getId();
        this.lastFM = lastFM;
        spotify = SpotifySingleton.getInstance();
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();

        this.service = service;
    }

    public void hoardUsers() {


        Date weekStart = week.getWeekStart();
        LocalDate l = weekStart.toLocalDate();
        LocalDateTime weekBeginning = l.minus(1, ChronoUnit.WEEKS).atStartOfDay();
        users.stream()
                .filter(usersWrapper -> !usersBeingProcessed.contains(usersWrapper.getDiscordID()))
                .filter(usersWrapper -> service.getUserData(weekId, usersWrapper.getLastFMName()).isEmpty())
                .forEach(usersWrapper -> {
                    try {
                        usersBeingProcessed.add(usersWrapper.getDiscordID());
                        String lastFMName = usersWrapper.getLastFMName();
                        TimeZone t = usersWrapper.getTimeZone();
                        List<TrackWithArtistId> tracksAndTimestamps = lastFM.getWeeklyBillboard(lastFMName,
                                (int) OffsetDateTime.of(weekBeginning, ZoneOffset.ofTotalSeconds(usersWrapper.getTimeZone().getOffset(Calendar.getInstance().getTimeInMillis()) / 1000)).toInstant().getEpochSecond()
                                , (int) OffsetDateTime.of(weekStart.toLocalDate().atStartOfDay(), ZoneOffset.ofTotalSeconds(usersWrapper.getTimeZone().getOffset(Calendar.getInstance().getTimeInMillis()) / 1000)).toInstant().getEpochSecond()
                        );
                        doArtistValidation(tracksAndTimestamps);
                        if (!tracksAndTimestamps.isEmpty())
                            service.insertUserData(weekId, lastFMName, tracksAndTimestamps);

                    } catch (LastFmException ignored) {
                    } finally {
                        usersBeingProcessed.remove(usersWrapper.getDiscordID());
                    }
                });

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

