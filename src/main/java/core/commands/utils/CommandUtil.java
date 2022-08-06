package core.commands.utils;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import core.commands.Context;
import core.exceptions.DiscogsServiceException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import core.services.validators.ArtistValidator;
import core.util.ChuuVirtualPool;
import core.util.VirtualParallel;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class CommandUtil {

    public static final Random rand = new Random();
    private static final ExecutorService logSupply = ChuuVirtualPool.of("Log-Supplier");


    private CommandUtil() {
    }

    public static boolean notEnoughPerms(Context e) {
        Member member = e.getMember();
        return member == null || !member.hasPermission(Permission.MESSAGE_MANAGE);
    }

    public static String notEnoughPermsTemplate() {
        return "Only a member with permission: **%s can ".formatted(Permission.MESSAGE_MANAGE);
    }

    public static String secondFormatter(int seconds) {
        int hours = seconds / 3600;
        int minutes = seconds / 60 % 60;
        if (hours == 0) {
            return minutes + CommandUtil.singlePlural(minutes, " minute", " minutes");
        }
        return String.format("%d:%02d hours", hours, minutes);
    }

    public static String noImageUrl(String artist) {
        return artist == null || artist
                .isEmpty() ? "https://lastfm-img2.akamaized.net/i/u/174s/4128a6eb29f94943c9d206c08e625904" : artist;
    }

    public static Color randomColor(Context event) {
        return ColorService.computeColor(event);
    }

    public static Color pastelColor() {
        double r = rand.nextFloat() / 2f + 0.5;
        double g = rand.nextFloat() / 2f + 0.5;
        double b = rand.nextFloat() / 2f + 0.5;
        return new Color((float) r, (float) g, (float) b);
    }


    public static BufferedImage getLogo(ChuuService dao, Context e) {
        try (var is = dao.findLogo(e.getGuild().getIdLong());
             var stream = is == null ? null : new BufferedInputStream(is)) {
            if (stream != null)
                return ImageIO.read(stream);
        } catch (IOException ex) {
            return null;
        }
        return null;
    }

    public static String updateUrl(DiscogsApi discogsApi, @Nonnull ScrobbledArtist scrobbledArtist, ChuuService dao, Spotify spotify) {
        String newUrl = null;
        try {
            newUrl = discogsApi.findArtistImage(scrobbledArtist.getArtist());
            VirtualParallel.handleInterrupt();
            if (!newUrl.isEmpty()) {
                dao.upsertUrl(newUrl, scrobbledArtist.getArtistId());
                VirtualParallel.handleInterrupt();
            } else {
                Pair<String, String> urlAndId = spotify.getUrlAndId(scrobbledArtist.getArtist());
                VirtualParallel.handleInterrupt();
                newUrl = urlAndId.getLeft();
                if (newUrl.isBlank()) {
                    scrobbledArtist.setUrl("");
                    scrobbledArtist.setUpdateBit(false);
                    dao.upsertArtistSad(scrobbledArtist);
                    VirtualParallel.handleInterrupt();
                } else {
                    dao.upsertSpotify(newUrl, scrobbledArtist.getArtistId(), urlAndId.getRight());
                    VirtualParallel.handleInterrupt();
                }
            }
        } catch (DiscogsServiceException ignored) {
            //Do nothing
        }
        return newUrl;
    }


    public static void validate(ChuuService dao, ScrobbledArtist scrobbledArtist, ConcurrentLastFM lastFM, DiscogsApi discogsApi, Spotify spotify) throws LastFmException {
        validate(dao, scrobbledArtist, lastFM, discogsApi, spotify, true, true);
    }


    public static void validate(ChuuService dao, ScrobbledArtist scrobbledArtist, ConcurrentLastFM lastFM, DiscogsApi discogsApi, Spotify spotify, boolean doUrlCheck, boolean findCorrection) throws LastFmException {
        if (findCorrection) {
            String dbCorrection = dao.findCorrection(scrobbledArtist.getArtist());
            if (dbCorrection != null) {
                scrobbledArtist.setArtist(dbCorrection);
            }
        }
        boolean existed;
        boolean corrected = false;
        //Find by id//
        // Doesnt exist? -> search for lastfm correction
        UpdaterStatus status = null;
        try {
            status = dao.getUpdaterStatusByName(scrobbledArtist.getArtist());
            scrobbledArtist.setArtistId(status.getArtistId());
            scrobbledArtist.setArtist(status.getArtistName());
            existed = true;
        } catch (InstanceNotFoundException e) {
            //Artist doesnt exists
            String originalArtist = scrobbledArtist.getArtist();
            String correction = lastFM.getCorrection(originalArtist);
            if (!correction.equalsIgnoreCase(originalArtist)) {
                scrobbledArtist.setArtist(correction);
                corrected = true;
            }
            try {
                status = dao.getUpdaterStatusByName(correction);
                scrobbledArtist.setArtistId(status.getArtistId());
                scrobbledArtist.setArtist(status.getArtistName());
                existed = true;
            } catch (InstanceNotFoundException ex) {
                scrobbledArtist.setArtist(correction);
                //Mutates id
                dao.upsertArtistSad(scrobbledArtist);
                existed = false;
            }
            if (corrected) {
                dao.insertCorrection(scrobbledArtist.getArtistId(), originalArtist);
            }


        }
        if (doUrlCheck) {
            if (!existed || (status.getArtistUrl() == null)) {
                if (discogsApi != null && spotify != null) {
                    scrobbledArtist.setUrl(CommandUtil.updateUrl(discogsApi, scrobbledArtist, dao, spotify));
                } else {
                    scrobbledArtist.setUrl(null);
                    return;
                }
            } else {
                scrobbledArtist.setUrl(status.getArtistUrl());
                dao.findArtistUrlAbove(scrobbledArtist.getArtistId(), 10).ifPresent(scrobbledArtist::setUrl);
            }
            if (scrobbledArtist.getUrl() == null || scrobbledArtist.getUrl().isBlank()) {
                scrobbledArtist.setUrl(null);
            }
        }

    }

    public static Album albumvalidate(ChuuService dao, ScrobbledArtist scrobbledArtist, ConcurrentLastFM lastFM, String album) throws LastFmException {
        try {
            return dao.getAlbum(scrobbledArtist.getArtistId(), album);
        } catch (InstanceNotFoundException exception) {
            FullAlbumEntityExtended chuu = lastFM.getAlbumSummary(LastFMData.ofDefault(), scrobbledArtist.getArtist(), album);
            ScrobbledAlbum scrobbledAlbum = new ScrobbledAlbum(chuu.getAlbum(), chuu.getArtist(), chuu.getAlbumUrl(), chuu.getMbid());
            scrobbledAlbum.setArtistId(scrobbledArtist.getArtistId());
            dao.insertAlbum(scrobbledAlbum);
            return new Album(scrobbledAlbum.getAlbumId(), scrobbledArtist.getArtistId(), chuu.getAlbum(), chuu.getAlbumUrl(), null, null, null);
        }
    }


    public static String albumUrl(ChuuService dao, ConcurrentLastFM lastFM, String artist, String album, DiscogsApi discogsApi, Spotify spotifyApi) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        validate(dao, scrobbledArtist, lastFM, discogsApi, spotifyApi);
        return albumUrl(dao, lastFM, scrobbledArtist.getArtistId(), scrobbledArtist.getArtist(), album);
    }

    public static String albumUrl(ChuuService dao, ConcurrentLastFM lastFM, long artistId, String artist, String album) throws LastFmException {
        try {
            return dao.findAlbumUrlByName(artistId, album);
        } catch (InstanceNotFoundException exception) {
            try {
                FullAlbumEntityExtended chuu = lastFM.getAlbumSummary(LastFMData.ofDefault(), artist, album);
                ScrobbledAlbum scrobbledAlbum = new ScrobbledAlbum(album, artist, chuu.getAlbumUrl(), chuu.getMbid());
                scrobbledAlbum.setArtistId(artistId);
                dao.insertAlbum(scrobbledAlbum);
                return scrobbledAlbum.getUrl();
            } catch (LastFmEntityNotFoundException e) {
                return null;
            }
        }
    }


    public static ScrobbledAlbum validateAlbum(ChuuService dao, String artist, String albumName, ConcurrentLastFM lastFM, DiscogsApi discogsApi, Spotify spotify, boolean doUrlCheck, boolean findCorrection) throws LastFmException {
        ScrobbledArtist sA = new ArtistValidator(dao, lastFM, null).validate(artist, false, findCorrection);
        Album album = CommandUtil.albumvalidate(dao, sA, lastFM, albumName);
        return new ScrobbledAlbum(album, sA.getArtist());
    }

    public static ScrobbledAlbum validateAlbum(ChuuService dao, long artistId, String artist, String albumName, ConcurrentLastFM lastFM) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        scrobbledArtist.setArtistId(artistId);
        Album album = CommandUtil.albumvalidate(dao, scrobbledArtist, lastFM, albumName);
        return new ScrobbledAlbum(album, scrobbledArtist.getArtist());
    }

    public static ScrobbledAlbum lightAlbumValidate(ChuuService dao, String artist, String albumName, ConcurrentLastFM lastFM) throws LastFmException {
        ScrobbledArtist sA = new ArtistValidator(dao, lastFM, null).validate(artist, false, true);
        Album album = CommandUtil.albumvalidate(dao, sA, lastFM, albumName);
        return new ScrobbledAlbum(album, sA.getArtist());
    }


    public static String getLastFmUser(String username) {
        return "https://www.last.fm/user/" + encodeUrl(Chuu.getLastFmId(username));
    }

    public static String singlePlural(long count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }


    public static String getGlobalUsername(long discordID) {
        return getGlobalUsername(null, discordID);
    }

    public static String getGlobalUsername(@Nullable Guild guild, long discordID) {
        return CommandUtil.escapeMarkdown(handleUser(guild, discordID, false).username());
    }

    public static int getDecade(int year) {
        if (year < 1900) {
            return (year / 10 * 10);
        }
        return year < 2000 ? (year / 10 * 10 - 1900) : (year / 10 * 10);
    }

    // ugh
    private static DiscordUserDisplay handleUser(@Nullable Guild g, long discordID, boolean isFromServer) {
        User user;
        String username;
        if (g != null) {
            Member whoD = g.getMemberById(discordID);
            if (whoD == null) {
                if (isFromServer) {
                    whoD = g.retrieveMemberById(discordID).onErrorFlatMap((t) -> new CompletedRestAction<>(g.getJDA(), null, null)).complete();
                    VirtualParallel.handleInterrupt();
                }
                if (whoD == null) {
                    user = core.Chuu.getShardManager().retrieveUserById(discordID).complete();
                    VirtualParallel.handleInterrupt();
                    username = user.getName();
                } else {
                    user = whoD.getUser();
                    username = whoD.getEffectiveName();
                }
            } else {
                username = whoD.getEffectiveName();
                user = whoD.getUser();
            }
        } else {
            user = core.Chuu.getShardManager().retrieveUserById(discordID).complete();
            VirtualParallel.handleInterrupt();
            username = user.getName();

        }
        return new DiscordUserDisplay((username), user.getAvatarUrl());

    }

    public static DiscordUserDisplay getUserInfoUnescaped(Context e, long discordID, boolean isFromServer) {
        return handleUser(e.isFromGuild() ? e.getGuild() : null, discordID, isFromServer);
    }

    public static DiscordUserDisplay getUserInfoUnescaped(Context e, long discordID) {
        return getUserInfoUnescaped(e, discordID, true);
    }

    public static DiscordUserDisplay getUserInfoUnescaped(long discordID) {
        return handleUser(null, discordID, false);
    }


    public static DiscordUserDisplay getUserInfoEscaped(Context e, long discordID) {
        return getUserInfoEscaped(e, discordID, true);
    }

    public static DiscordUserDisplay getUserInfoEscaped(Context e, long discordID, boolean isFromServer) {
        DiscordUserDisplay discordUserDisplay = handleUser(e.isFromGuild() ? e.getGuild() : null, discordID, isFromServer);
        return new DiscordUserDisplay(escapeMarkdown(discordUserDisplay.username()), discordUserDisplay.urlImage());
    }


    public static char getMessagePrefix(Context e) {
        return e.getPrefix();
    }

    public static String encodeUrl(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    public static String escapeMarkdown(String string) {
        return LinkUtils.markdownStripper.matcher(string).replaceAll("\\\\$1");
    }

    public static String stripEscapedMarkdown(String string) {
        return LinkUtils.stripEscapedMarkdown(string);

    }

    public static String unescapedUser(String string, long discordId, Context e) {
        return getUserInfoUnescaped(e, discordId).username();
    }

    public static void handleConditionalMessage(CompletableFuture<Message> future) {
        if (future == null) {
            return;
        }
        if (!future.isDone()) {
            future.cancel(true);
            return;
        }
        future.join().delete().queue();
    }

    public static String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        return switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    public static String getRank(long rank) {
        long l = rank % 100;
        if (l >= 11 && l <= 13) {
            return "th";
        }
        l = rank % 10L;
        return switch (Math.toIntExact(l)) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    public static RemainingImagesMode getEffectiveMode(RemainingImagesMode remainingImagesMode, CommandParameters params) {
        boolean pie = params.hasOptional("pie");
        boolean list = params.hasOptional("list");
        if ((remainingImagesMode.equals(RemainingImagesMode.LIST) && !list && !pie) || (!remainingImagesMode.equals(RemainingImagesMode.LIST) && list)) {
            return RemainingImagesMode.LIST;
        } else if (remainingImagesMode.equals(RemainingImagesMode.PIE) && !pie || !remainingImagesMode.equals(RemainingImagesMode.PIE) && pie) {
            return RemainingImagesMode.PIE;
        } else {
            return RemainingImagesMode.IMAGE;
        }
    }

    public static String getDateTimestampt(Instant instant) {
        return getDateTimestampt(instant, TimeFormat.DATE_TIME_SHORT);
    }

    public static String getDateTimestampt(Instant instant, TimeFormat timeFormat) {
        return timeFormat.format(instant);
    }

    public static String getAmericanizedDate(OffsetDateTime offsetDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d");

        String day = formatter.format(offsetDateTime);
        String first = DateTimeFormatter.ofPattern("HH:mm 'on' MMMM").format(offsetDateTime);
        String year = DateTimeFormatter.ofPattern("yyyy").format(offsetDateTime);

        return String.format("%s %s%s, %s", first, day, CommandUtil.getDayNumberSuffix(Integer.parseInt(day)), year);
    }

    public static boolean showBottedAccounts(@Nullable LastFMData lastfmData, CommandParameters parameters, ChuuService chuuService) {
        if (parameters.hasOptional("nobotted")) {
            return false;
        }
        if (parameters.hasOptional("botted")) {
            return true;
        }
        if (lastfmData == null) {

            try {
                return chuuService.findLastFMData(parameters.getE().getAuthor().getIdLong()).isShowBotted();
            } catch (InstanceNotFoundException instanceNotFoundException) {
                return true;
            }
        }
        return lastfmData.isShowBotted();

    }


    public static String msToString(long ms) {
        var s = ms / 1000;
        var m = s / 60;
        var h = m / 60;

        if (h > 0) {
            return String.format("%02d:%02d:%02d", h, m % 60, s % 60);
        } else {
            return String.format("%02d:%02d", m, s % 60);
        }
    }

    public static GlobalStreakEntities.DateHolder toDateHolder(Instant streakStart, String lastfmId) {

        if (streakStart.isBefore(Instant.EPOCH.plus(60 * 60 * 24 * 365, ChronoUnit.SECONDS))) {
            return new GlobalStreakEntities.DateHolder(streakStart, "-", PrivacyUtils.getLastFmUser(lastfmId));
        } else {
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(streakStart, ZoneId.of("UTC"));

            String day = offsetDateTime.toLocalDate().format(DateTimeFormatter.ISO_DATE);
            String date = CommandUtil.getDateTimestampt(streakStart, TimeFormat.RELATIVE);
            String link = String.format("%s/library?from=%s&rangetype=1day", PrivacyUtils.getLastFmUser(lastfmId), day);
            return new GlobalStreakEntities.DateHolder(streakStart, date, link);

        }
    }

    public static <T> CompletableFuture<T> supplyLog(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, logSupply).whenComplete((u, ex) -> {
            if (ex != null) {
                Chuu.getLogger().warn(ex.getMessage(), ex);
            }
        });
    }

    public static CompletableFuture<Void> runLog(Runnable supplier) {
        return CompletableFuture.runAsync(supplier, logSupply).whenComplete((u, ex) -> {
            if (ex != null) {
                Chuu.getLogger().warn(ex.getMessage(), ex);
            }
        });
    }


}
