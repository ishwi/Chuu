package core.services;

import com.neovisionaries.i18n.CountryCode;
import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.TopEntity;
import core.commands.CommandUtil;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.NPMode;
import dao.ChuuService;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static core.commands.AlbumRatings.getStartsFromScore;

public class NPModeBuilder {
    static {
        try {
            List<Object> tags = List.of(NPMode.TAGS, 0,
                    NPMode.CROWN, 1,
                    NPMode.ARTIST_RANK, 2,
                    NPMode.GLOBAL_CROWN, 3,
                    NPMode.GLOBAL_RANK, 4,
                    NPMode.ALBUM_CROWN, 5,
                    NPMode.ALBUM_RANK, 6,
                    NPMode.GLOBAL_ALBUM_CROWN, 7,
                    NPMode.GLOBAL_ALBUM_RANK, 8,
                    NPMode.LFM_LISTENERS, 9,
                    NPMode.LFM_SCROBBLES, 10,
                    NPMode.BOT_LISTENERS, 11,
                    NPMode.BOT_SCROBBLES, 12,
                    NPMode.SERVER_LISTENERS, 13,
                    NPMode.SERVER_SCROBBLES, 14,
                    NPMode.GENDER, 15,
                    NPMode.COUNTRY, 16,
                    NPMode.BOT_ALBUM_RYM, 17,
                    NPMode.SERVER_ALBUM_RYM, 18,
                    NPMode.ALBUM_RYM, 19);

            Map<NPMode, Integer> temp = new HashMap<>();
            for (int i = 0; i < tags.size(); i += 2) {
                Object np = tags.get(i);
                Object c = tags.get(i + 1);
                assert np instanceof NPMode;
                assert c instanceof Integer;
                temp.put((NPMode) np, (Integer) c);
            }
            footerIndexes = temp;
        } catch (Exception e) {
            throw new RuntimeException("Could not init class.", e);
        }


    }

    static final Map<NPMode, Integer> footerIndexes;


    private final NowPlayingArtist np;
    private final MessageReceivedEvent e;
    private String[] footerSpaces;
    private final long discordId;
    private final String userName;
    private final EnumSet<NPMode> npModes;
    private final String lastFMName;
    private final EmbedBuilder embedBuilder;
    private final ScrobbledArtist scrobbledArtist;
    private final Long albumId;
    private final ChuuService service;
    private final ConcurrentLastFM lastFM;
    private final String serverName;
    private final MusicBrainzService mb;
    private List<String> outputList;

    public NPModeBuilder(NowPlayingArtist np, MessageReceivedEvent e, String[] footerSpaces, long discordId, String userName, EnumSet<NPMode> npModes, String lastFMName, EmbedBuilder embedBuilder, ScrobbledArtist scrobbledArtist, Long albumId, ChuuService service, ConcurrentLastFM lastFM, String serverName, MusicBrainzService mb, List<String> outputList) {
        this.np = np;
        this.e = e;
        this.footerSpaces = footerSpaces;
        this.discordId = discordId;
        this.userName = userName;
        this.npModes = npModes;
        this.lastFMName = lastFMName;
        this.embedBuilder = embedBuilder;
        this.scrobbledArtist = scrobbledArtist;
        this.albumId = albumId;
        this.service = service;
        this.lastFM = lastFM;
        this.serverName = serverName;
        this.mb = mb;
        this.outputList = outputList;
    }

    public static int getSize() {
        return footerIndexes.size();
    }

    public CompletableFuture<?> buildNp() {
        Set<Integer> previousNewLinesToAdd = new HashSet<>();
        List<CompletableFuture<?>> completableFutures = new ArrayList<>();
        UnaryOperator<CompletableFuture<?>> logger = (x) -> x.whenComplete((u, ex) -> {
            if (ex != null) {
                Chuu.getLogger().warn(ex.getMessage(), ex);
            }
        });


        AtomicBoolean whoKnowsLock = new AtomicBoolean(false);
        AtomicBoolean whoKnowsAlbumLock = new AtomicBoolean(false);
        AtomicBoolean globalWhoKnowsLock = new AtomicBoolean(false);
        AtomicBoolean globalWhoKnowsAlbumLock = new AtomicBoolean(false);
        AtomicBoolean ratingLock = new AtomicBoolean(false);
        AtomicBoolean serverStats = new AtomicBoolean(false);
        AtomicBoolean botStats = new AtomicBoolean(false);
        AtomicBoolean lfmStats = new AtomicBoolean(false);
        AtomicBoolean mbLock = new AtomicBoolean(false);


        for (
                NPMode npMode : npModes) {
            Integer index = footerIndexes.get(npMode);
            assert index != null;
            long guildId = e.getGuild().getIdLong();
            switch (npMode) {
                case NORMAL:
                    break;
                case PREVIOUS:
                    completableFutures.add(logger.apply(CompletableFuture.runAsync(() -> {
                        try {
                            StringBuilder t = new StringBuilder();
                            NowPlayingArtist np = lastFM.getRecent(lastFMName, 2).get(1);
                            t.append("**").append(CommandUtil.cleanMarkdownCharacter(np.getArtistName()))
                                    .append("** | ").append(CommandUtil.cleanMarkdownCharacter(np.getAlbumName())).append("\n");
                            embedBuilder.addField("Previous: **" + np.getSongName() + "**", t.toString(), false);
                        } catch (LastFmException ignored) {
                        }
                    })));
                    break;
                case TAGS:
                    completableFutures.add(logger.apply(CompletableFuture.runAsync(() -> {
                        try {
                            Set<String> tags = new HashSet<>(5);
                            tags.addAll(lastFM.getTrackTags(1, TopEntity.TRACK, np.getArtistName(), np.getSongName()));
                            if (tags.size() < 5 && !np.getAlbumName().isBlank()) {
                                tags.addAll(lastFM.getTrackTags(1, TopEntity.ALBUM, np.getArtistName(), np.getAlbumName()));
                            }
                            if (tags.size() < 5) {
                                tags.addAll(lastFM.getTrackTags(5, TopEntity.ARTIST, np.getArtistName(), null));
                            }
                            if (!tags.isEmpty()) {
                                String tagsField = EmbedBuilder.ZERO_WIDTH_SPACE + " • " + String.join(" - ", tags);
                                tagsField += '\n';
                                footerSpaces[index] = tagsField;
                            }
                        } catch (LastFmException ignored) {
                        }
                    })));
                    break;
                case CROWN:
                case ARTIST_RANK:
                    if (e.isFromGuild()) {
                        if (!whoKnowsLock.compareAndSet(false, true)) {
                            break;
                        }
                        completableFutures.add(logger.apply(CompletableFuture.runAsync(() -> {
                            WrapperReturnNowPlaying wrapperReturnNowPlaying = service.whoKnows(scrobbledArtist.getArtistId(), guildId, 10_000);
                            List<ReturnNowPlaying> returnNowPlayings = wrapperReturnNowPlaying.getReturnNowPlayings();
                            if (!returnNowPlayings.isEmpty()) {

                                ReturnNowPlaying returnNowPlaying = returnNowPlayings.get(0);
                                String userString = CommandUtil.getUserInfoNotStripped(e, returnNowPlaying.getDiscordId()).getUsername();
                                if (npModes.contains(NPMode.CROWN))
                                    footerSpaces[footerIndexes.get(NPMode.CROWN)] =
                                            "\uD83D\uDC51 " + returnNowPlaying.getPlayNumber() + " (" + userString + ")";
                                if (npModes.contains(NPMode.ARTIST_RANK)) {
                                    for (int i = 0; i < returnNowPlayings.size(); i++) {
                                        ReturnNowPlaying searching = returnNowPlayings.get(i);
                                        if (searching.getDiscordId() == discordId) {
                                            footerSpaces[footerIndexes.get(NPMode.ARTIST_RANK)] = serverName + " Rank: " + (i + 1) + CommandUtil.getDayNumberSuffix(i + 1) + "/" + returnNowPlayings.size();
                                            if (npModes.contains(NPMode.CROWN))
                                                addNewLineToPrevious(footerIndexes.get(NPMode.CROWN));
                                            break;
                                        }
                                    }
                                }
                            }
                        })));
                    }
                    break;
                case ALBUM_CROWN:
                case ALBUM_RANK:
                    if (e.isFromGuild() && albumId != null) {
                        if (!whoKnowsAlbumLock.compareAndSet(false, true)) {
                            break;
                        }
                        completableFutures.add(logger.apply(CompletableFuture.runAsync(() -> {
                            WrapperReturnNowPlaying wrapperReturnNowPlaying = service.getWhoKnowsAlbums(10_000, albumId, guildId);
                            List<ReturnNowPlaying> returnNowPlayings = wrapperReturnNowPlaying.getReturnNowPlayings();
                            if (!returnNowPlayings.isEmpty()) {

                                ReturnNowPlaying returnNowPlaying = returnNowPlayings.get(0);
                                String userString = CommandUtil.getUserInfoNotStripped(e, returnNowPlaying.getDiscordId()).getUsername();
                                if (npModes.contains(NPMode.ALBUM_CROWN))
                                    footerSpaces[footerIndexes.get(NPMode.ALBUM_CROWN)] =
                                            "Album \uD83D\uDC51 " + returnNowPlaying.getPlayNumber() + " (" + userString + ")";
                                if (npModes.contains(NPMode.ALBUM_RANK)) {
                                    for (int i = 0; i < returnNowPlayings.size(); i++) {
                                        ReturnNowPlaying searching = returnNowPlayings.get(i);
                                        if (searching.getDiscordId() == discordId) {
                                            footerSpaces[footerIndexes.get(NPMode.ALBUM_RANK)] = serverName + " Album Rank: " + (i + 1) + CommandUtil.getDayNumberSuffix(i + 1) + "/" + returnNowPlayings.size();
                                            if (npModes.contains(NPMode.ALBUM_CROWN))
                                                addNewLineToPrevious(footerIndexes.get(NPMode.ALBUM_CROWN));
                                            break;
                                        }

                                    }
                                }
                            }
                        })));
                    }
                case SERVER_LISTENERS:
                case SERVER_SCROBBLES:
                    if (e.isFromGuild()) {
                        if (!serverStats.compareAndSet(false, true)) {
                            break;
                        }
                        completableFutures.add(logger.apply(CompletableFuture.runAsync(() -> {
                            if (npModes.contains(NPMode.BOT_LISTENERS)) {
                                long artistFrequencies = service.getArtistFrequencies(guildId, scrobbledArtist.getArtistId());
                                footerSpaces[footerIndexes.get(NPMode.BOT_LISTENERS)] =
                                        (String.format("%d %s listeners", artistFrequencies, serverName));
                            }
                            if (npModes.contains(NPMode.BOT_SCROBBLES)) {
                                long serverArtistPlays = service.getServerArtistPlays(guildId, scrobbledArtist.getArtistId());
                                footerSpaces[footerIndexes.get(NPMode.BOT_SCROBBLES)] =
                                        (String.format("%d %s plays", serverArtistPlays, serverName));
                                if (npModes.contains(NPMode.BOT_LISTENERS)) {
                                    addNewLineToPrevious(footerIndexes.get(NPMode.BOT_SCROBBLES));
                                }
                            }
                        })));
                    }
                    break;
                case LFM_LISTENERS:
                case LFM_SCROBBLES:
                    if (!lfmStats.compareAndSet(false, true)) {
                        break;
                    }
                    completableFutures.add(logger.apply(CompletableFuture.runAsync(() -> {
                        try {
                            ArtistSummary summary = lastFM.getArtistSummary(scrobbledArtist.getArtist(), lastFMName);
                            long artistFrequencies = summary.getListeners();
                            long serverArtistPlays = summary.getPlaycount();
                            if (npModes.contains(NPMode.LFM_LISTENERS))
                                footerSpaces[footerIndexes.get(NPMode.LFM_LISTENERS)] =
                                        (String.format("%d %s listeners", artistFrequencies, "Last.fm"));
                            if (npModes.contains(NPMode.LFM_SCROBBLES)) {
                                footerSpaces[footerIndexes.get(NPMode.LFM_SCROBBLES)] =
                                        (String.format("%d %s plays", serverArtistPlays, "Last.fm"));
                                if (npModes.contains(NPMode.LFM_LISTENERS)) {
                                    addNewLineToPrevious(footerIndexes.get(NPMode.LFM_SCROBBLES));
                                }
                            }
                        } catch (LastFmException ignored) {
                        }


                    })));
                    break;

                case ALBUM_RYM:
                case SERVER_ALBUM_RYM:
                case BOT_ALBUM_RYM:
                    if (!ratingLock.compareAndSet(false, true)) {
                        break;
                    }
                    if (albumId != null) {
                        completableFutures.add(logger.apply(CompletableFuture.runAsync(() -> {
                            NumberFormat average = new DecimalFormat("#0.##");

                            if ((npModes.contains(NPMode.SERVER_ALBUM_RYM) || npModes.contains(NPMode.BOT_ALBUM_RYM) || npModes.contains(NPMode.ALBUM_RYM) && (npModes.contains(NPMode.SERVER_ALBUM_RYM) || npModes.contains(NPMode.BOT_ALBUM_RYM)))) {

                                AlbumRatings albumRatings = service.getRatingsByName(e.isFromGuild() ? guildId : -1L, np.getAlbumName(), scrobbledArtist.getArtistId());
                                List<Rating> userRatings = albumRatings.getUserRatings();
                                if (npModes.contains(NPMode.SERVER_ALBUM_RYM)) {
                                    List<Rating> serverList = userRatings.stream().filter(Rating::isSameGuild).collect(Collectors.toList());
                                    footerSpaces[footerIndexes.get(NPMode.SERVER_ALBUM_RYM)] =
                                            (String.format("%s Average: %s | Ratings: %d", serverName, average.format(serverList.stream().mapToDouble(rating -> rating.getRating() / 2f).average().orElse(0)), serverList.size()));
                                }
                                if (npModes.contains(NPMode.BOT_ALBUM_RYM)) {
                                    footerSpaces[footerIndexes.get(NPMode.BOT_ALBUM_RYM)] =
                                            (String.format("%s Average: %s | Ratings: %d", e.getJDA().getSelfUser().getName()
                                                    , average.format(userRatings.stream().mapToDouble(rating -> rating.getRating() / 2f).average().orElse(0)), userRatings.size()));

                                }
                                if (npModes.contains(NPMode.ALBUM_RYM)) {
                                    Optional<Rating> first = userRatings.stream().filter(x -> x.getDiscordId() == discordId).findFirst();
                                    first.ifPresent(rating -> {
                                        previousNewLinesToAdd.add(footerIndexes.get(NPMode.ALBUM_RYM));
                                        footerSpaces[footerIndexes.get(NPMode.ALBUM_RYM)] = userName + ": " + getStartsFromScore().apply(rating.getRating());
                                    });
                                }
                            } else if (npModes.contains(NPMode.ALBUM_RYM) && !npModes.contains(NPMode.BOT_ALBUM_RYM) && !npModes.contains(NPMode.SERVER_ALBUM_RYM)) {
                                Rating rating = service.getUserAlbumRating(discordId, albumId, scrobbledArtist.getArtistId());
                                if (rating != null) {
                                    footerSpaces[footerIndexes.get(NPMode.ALBUM_RYM)] = userName + ": " + getStartsFromScore().apply(rating.getRating());
                                    previousNewLinesToAdd.add(footerIndexes.get(NPMode.ALBUM_RYM));
                                }
                            }
                        })));
                    }
                    break;
                case GLOBAL_CROWN:
                case GLOBAL_RANK:
                    if (!globalWhoKnowsLock.compareAndSet(false, true)) {
                        break;
                    }
                    completableFutures.add(logger.apply(CompletableFuture.runAsync(() -> {
                        List<GlobalCrown> globalArtistRanking = service.getGlobalArtistRanking(scrobbledArtist.getArtistId(), false, discordId);
                        if (!globalArtistRanking.isEmpty()) {

                            GlobalCrown returnNowPlaying = globalArtistRanking.get(0);


                            if (npModes.contains(NPMode.GLOBAL_CROWN)) {
                                String holder = null;
                                try {
                                    LastFMData lastFMData = service.findLastFMData(globalArtistRanking.get(0).getDiscordId());

                                    if (EnumSet.of(PrivacyMode.LAST_NAME, PrivacyMode.TAG, PrivacyMode.DISCORD_NAME).contains(lastFMData.getPrivacyMode())) {

                                        switch (lastFMData.getPrivacyMode()) {
                                            case DISCORD_NAME:
                                                holder = CommandUtil.getUserInfoNotStripped(e, returnNowPlaying.getDiscordId()).getUsername();
                                                break;
                                            case TAG:
                                                holder = e.getJDA().retrieveUserById(lastFMData.getDiscordId()).complete().getAsTag();
                                                break;
                                            case LAST_NAME:
                                                holder = lastFMData.getName() + " (lastfm)";
                                                break;
                                            default:
                                                holder =
                                                        "Private User #1";
                                        }
                                    } else holder = "Private User #1";
                                } catch (InstanceNotFoundException exception) {
                                    exception.printStackTrace();
                                }
                                footerSpaces[footerIndexes.get(NPMode.GLOBAL_CROWN)] =
                                        "Global \uD83D\uDC51 " + returnNowPlaying.getPlaycount() + " (" + holder + ")";
                            }
                            if (npModes.contains(NPMode.GLOBAL_RANK)) {
                                Optional<GlobalCrown> yourPosition = globalArtistRanking.stream().filter(x -> x.getDiscordId() == discordId).findFirst();
                                yourPosition.
                                        map(gc -> "Global Rank: " + gc.getRanking() + CommandUtil.getDayNumberSuffix(gc.getRanking()) + "/" + globalArtistRanking.size())
                                        .map(x -> {
                                            if (npModes.contains(NPMode.GLOBAL_CROWN)) {
                                                previousNewLinesToAdd.add(footerIndexes.get(NPMode.GLOBAL_CROWN));
                                            }
                                            return x;
                                        })
                                        .ifPresent(s -> footerSpaces[footerIndexes.get(NPMode.GLOBAL_RANK)] = s);

                            }
                        }
                    })));
                    break;
                case GLOBAL_ALBUM_CROWN:
                case GLOBAL_ALBUM_RANK:
                    if (albumId != null) {
                        if (!globalWhoKnowsAlbumLock.compareAndSet(false, true)) {
                            break;
                        }
                        completableFutures.add(logger.apply(CompletableFuture.runAsync(() -> {
                            WrapperReturnNowPlaying wrapperReturnNowPlaying = service.getGlobalWhoKnowsAlbum(10_000, albumId, discordId, false);
                            List<ReturnNowPlaying> returnNowPlayings = wrapperReturnNowPlaying.getReturnNowPlayings();
                            if (!returnNowPlayings.isEmpty()) {

                                ReturnNowPlaying returnNowPlaying = returnNowPlayings.get(0);
                                String userString = CommandUtil.getUserInfoNotStripped(e, returnNowPlaying.getDiscordId()).getUsername();
                                if (npModes.contains(NPMode.GLOBAL_ALBUM_CROWN))
                                    footerSpaces[footerIndexes.get(NPMode.GLOBAL_ALBUM_CROWN)] =
                                            "Global Album \uD83D\uDC51 " + returnNowPlaying.getPlayNumber() + " (" + userString + ")";
                                if (npModes.contains(NPMode.GLOBAL_ALBUM_RANK)) {
                                    for (int i = 0; i < returnNowPlayings.size(); i++) {
                                        ReturnNowPlaying searching = returnNowPlayings.get(i);
                                        if (searching.getDiscordId() == discordId) {
                                            footerSpaces[footerIndexes.get(NPMode.GLOBAL_ALBUM_RANK)] = "Global Album Rank: " + (i + 1) + CommandUtil.getDayNumberSuffix(i + 1) + "/" + returnNowPlayings.size();

                                            if (npModes.contains(NPMode.GLOBAL_ALBUM_RANK)) {
                                                previousNewLinesToAdd.add(footerIndexes.get(NPMode.GLOBAL_ALBUM_CROWN));
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        })));
                    }
                    break;
                case BOT_LISTENERS:
                case BOT_SCROBBLES:
                    if (!botStats.compareAndSet(false, true)) {
                        break;
                    }
                    completableFutures.add(logger.apply(CompletableFuture.runAsync(() -> {
                        String name = e.getJDA().getSelfUser().getName();
                        if (npModes.contains(NPMode.BOT_LISTENERS)) {
                            long artistFrequencies = service.getGlobalArtistFrequencies(scrobbledArtist.getArtistId());
                            footerSpaces[footerIndexes.get(NPMode.BOT_LISTENERS)] =
                                    (String.format("%d %s listeners", artistFrequencies, name));
                        }
                        if (npModes.contains(NPMode.BOT_SCROBBLES)) {
                            long artistFrequencies = service.getGlobalArtistPlays(scrobbledArtist.getArtistId());
                            footerSpaces[footerIndexes.get(NPMode.BOT_SCROBBLES)] =
                                    (String.format("%d %s plays", artistFrequencies, name));
                        }
                    })));

                    break;
                case GENDER:
                case COUNTRY:
                    if (!mbLock.compareAndSet(false, true)) {
                        break;
                    }
                    completableFutures.add(logger.apply(CompletableFuture.runAsync(() -> {
                        ArtistMusicBrainzDetails artistDetails = mb.getArtistDetails(new ArtistInfo(null, np.getArtistName(), np.getArtistMbid()));
                        if (npModes.contains(NPMode.GENDER) && artistDetails != null && artistDetails.getGender() != null) {
                            footerSpaces[footerIndexes.get(NPMode.GENDER)] =
                                    artistDetails.getGender();
                        }
                        if (npModes.contains(NPMode.COUNTRY) && artistDetails != null && artistDetails.getCountryCode() != null) {
                            footerSpaces[footerIndexes.get(NPMode.BOT_SCROBBLES)] =
                                    CountryCode.getByAlpha2Code(artistDetails.getCountryCode()).getName();
                        }
                    })));

                    break;
                case ARTIST_PIC:
                    break;
                case UNKNOWN:
                    throw new IllegalStateException();
            }
        }
        return CompletableFuture.allOf(completableFutures.toArray(CompletableFuture<?>[]::new)).exceptionally(x -> null).thenAccept((x) -> {
            previousNewLinesToAdd.forEach(this::addNewLineToPrevious);
            List<String> collect = Arrays.stream(footerSpaces).filter(Objects::nonNull).collect(Collectors.toList());
            boolean even = collect.size() % 2 == 0;

            int counter = 0;
            for (int i = 0; i < collect.size(); i++) {
                if (npModes.contains(NPMode.TAGS) && (i == 0)) {
                    continue;
                }
                Predicate<Integer> tester = (npModes.contains(NPMode.TAGS) ? (j) -> j % 2 != 0 : (j) -> j % 2 == 0);
                String current = collect.get(i);
                if (tester.test(i)) {
                    if (current.endsWith("\n")) {
                        collect.set(i, current.substring(0, current.length() - 1));
                    }
                } else {
                    if (!current.endsWith("\n")) {
                        collect.set(i, current + "\n");
                    }
                }
            }
            outputList.addAll(collect);
        });
    }

    public void addNewLineToPrevious(int currentIndex) {
        while (--currentIndex >= 0) {
            String footerSpace = footerSpaces[currentIndex];
            if (footerSpace == null) {
                continue;
            }
            if (!footerSpace.endsWith("\n")) {
                footerSpaces[currentIndex] += "\n";
                break;
            }
        }
    }
}
