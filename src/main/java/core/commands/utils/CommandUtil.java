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
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class CommandUtil {

    public static final Random rand = new Random();

    private CommandUtil() {
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
        try (InputStream stream = dao.findLogo(e.getGuild().getIdLong())) {
            if (stream != null)
                return ImageIO.read(stream);
        } catch (IOException ex) {
            return null;
        }
        return null;
    }

    public static String updateUrl(DiscogsApi discogsApi, @NotNull ScrobbledArtist scrobbledArtist, ChuuService dao, Spotify spotify) {
        String newUrl = null;
        try {
            newUrl = discogsApi.findArtistImage(scrobbledArtist.getArtist());
            if (!newUrl.isEmpty()) {
                dao.upsertUrl(newUrl, scrobbledArtist.getArtistId());
            } else {
                Pair<String, String> urlAndId = spotify.getUrlAndId(scrobbledArtist.getArtist());
                newUrl = urlAndId.getLeft();
                if (newUrl.isBlank()) {
                    scrobbledArtist.setUrl("");
                    scrobbledArtist.setUpdateBit(false);
                    dao.upsertArtistSad(scrobbledArtist);
                } else {
                    dao.upsertSpotify(newUrl, scrobbledArtist.getArtistId(), urlAndId.getRight());
                }
            }
        } catch (DiscogsServiceException ignored) {
            //Do nothing
        }
        return newUrl;
    }

    public static String getArtistImageUrl(ChuuService dao, String artist, ConcurrentLastFM lastFM, DiscogsApi discogsApi, Spotify spotify) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(dao, scrobbledArtist, lastFM, discogsApi, spotify);
        return scrobbledArtist.getUrl();
    }


    public static void validate(ChuuService dao, ScrobbledArtist scrobbledArtist, ConcurrentLastFM lastFM, DiscogsApi discogsApi, Spotify spotify) throws LastFmException {
        validate(dao, scrobbledArtist, lastFM, discogsApi, spotify, true);
    }

    private static void validate(ChuuService dao, ScrobbledArtist scrobbledArtist, ConcurrentLastFM lastFM, DiscogsApi discogsApi, Spotify spotify, boolean doUrlCheck) throws LastFmException {
        validate(dao, scrobbledArtist, lastFM, discogsApi, spotify, doUrlCheck, true);
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
            if (!existed || (status.getArtistUrl() == null))
                scrobbledArtist.setUrl(CommandUtil.updateUrl(discogsApi, scrobbledArtist, dao, spotify));
            else {
                scrobbledArtist.setUrl(status.getArtistUrl());
//                dao.getServerSetUrlArtist()
                dao.findArtistUrlAbove(scrobbledArtist.getArtistId(), 10).ifPresent(scrobbledArtist::setUrl);
            }
            if (scrobbledArtist.getUrl() == null || scrobbledArtist.getUrl().isBlank()) {
                scrobbledArtist.setUrl(null);
            }
        }
    }

    public static long albumvalidate(ChuuService dao, ScrobbledArtist scrobbledArtist, ConcurrentLastFM lastFM, String album) throws LastFmException {
        try {
            return dao.findAlbumIdByName(scrobbledArtist.getArtistId(), album);
        } catch (InstanceNotFoundException exception) {
            FullAlbumEntityExtended chuu = lastFM.getAlbumSummary(LastFMData.ofDefault(), scrobbledArtist.getArtist(), album);
            ScrobbledAlbum scrobbledAlbum = new ScrobbledAlbum(chuu.getAlbum(), chuu.getArtist(), chuu.getAlbumUrl(), chuu.getMbid());
            scrobbledAlbum.setArtistId(scrobbledArtist.getArtistId());
            dao.insertAlbum(scrobbledAlbum);
            return scrobbledAlbum.getAlbumId();
        }
    }

    public static long trackValidate(ChuuService dao, ScrobbledArtist scrobbledArtist, ConcurrentLastFM lastFM, String track) throws LastFmException {
        try {
            return dao.findTrackIdByName(scrobbledArtist.getArtistId(), track);
        } catch (InstanceNotFoundException exception) {
            Track trackInfo = lastFM.getTrackInfo(LastFMData.ofDefault(), scrobbledArtist.getArtist(), track);
            ScrobbledTrack scrobbledTrack = new ScrobbledTrack(scrobbledArtist.getArtist(), track, 0, false, trackInfo.getDuration(), trackInfo.getImageUrl(), null, trackInfo.getMbid());
            scrobbledTrack.setArtistId(scrobbledArtist.getArtistId());
            dao.insertTrack(scrobbledTrack);
            return scrobbledTrack.getTrackId();
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

    public static ScrobbledArtist onlyCorrection(ChuuService dao, String artist, ConcurrentLastFM lastFM, boolean doCorrection) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        validate(dao, scrobbledArtist, lastFM, null, null, false, doCorrection);
        return scrobbledArtist;
    }

    public static ScrobbledAlbum validateAlbum(ChuuService dao, String artist, String album, ConcurrentLastFM lastFM, DiscogsApi discogsApi, Spotify spotify, boolean doUrlCheck, boolean findCorrection) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(dao, scrobbledArtist, lastFM, discogsApi, spotify, doUrlCheck, findCorrection);
        long albumvalidate = CommandUtil.albumvalidate(dao, scrobbledArtist, lastFM, album);
        ScrobbledAlbum scrobbledAlbum = new ScrobbledAlbum(album, scrobbledArtist.getArtist(), null, null);
        scrobbledAlbum.setArtistId(scrobbledArtist.getArtistId());
        scrobbledAlbum.setAlbumId(albumvalidate);
        return scrobbledAlbum;
    }

    public static ScrobbledAlbum validateAlbum(ChuuService dao, long artistId, String artist, String album, ConcurrentLastFM lastFM) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        scrobbledArtist.setArtistId(artistId);
        long albumvalidate = CommandUtil.albumvalidate(dao, scrobbledArtist, lastFM, album);
        ScrobbledAlbum scrobbledAlbum = new ScrobbledAlbum(album, scrobbledArtist.getArtist(), null, null);
        scrobbledAlbum.setArtistId(scrobbledArtist.getArtistId());
        scrobbledAlbum.setAlbumId(albumvalidate);
        return scrobbledAlbum;
    }

    public static ScrobbledAlbum lightAlbumValidate(ChuuService dao, String artist, String album, ConcurrentLastFM lastFM) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(dao, scrobbledArtist, lastFM, null, null, false, true);
        long albumvalidate = CommandUtil.albumvalidate(dao, scrobbledArtist, lastFM, album);

        ScrobbledAlbum scrobbledAlbum = new ScrobbledAlbum(album, scrobbledArtist.getArtist(), null, null);
        scrobbledAlbum.setArtistId(scrobbledArtist.getArtistId());
        scrobbledAlbum.setAlbumId(albumvalidate);
        return scrobbledAlbum;
    }


    public static String getLastFmUser(String username) {
        return "https://www.last.fm/user/" + encodeUrl(Chuu.getLastFmId(username));
    }

    public static String singlePlural(long count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }


    public static String getGlobalUsername(JDA jda, long discordID) {
        return CommandUtil.cleanMarkdownCharacter(jda.retrieveUserById(discordID, false).complete().getName());
    }

    public static int getDecade(int year) {
        if (year < 1900) {
            return (year / 10 * 10);
        }
        return year < 2000 ? (year / 10 * 10 - 1900) : (year / 10 * 10);
    }

    // ugh
    private static DiscordUserDisplay handleUser(Context e, long discordID) {
        User user;
        String username;
        if (e.isFromGuild()) {
            Member whoD = e.getGuild().getMemberById(discordID);
            if (whoD == null) {
                Member member = e.getGuild().retrieveMemberById(discordID).onErrorFlatMap((t) -> new CompletedRestAction<>(e.getJDA(), null, null)).complete();
                if (member == null) {
                    user = e.getJDA().retrieveUserById(discordID, false).complete();
                    username = user.getName();
                } else {
                    user = member.getUser();
                    username = member.getEffectiveName();
                }
            } else {
                username = whoD.getEffectiveName();
                user = whoD.getUser();
            }
        } else {
            user = e.getJDA().retrieveUserById(discordID, false).complete();
            username = user.getName();

        }
        return new DiscordUserDisplay((username), user.getAvatarUrl() == null || user.getAvatarUrl().isBlank() ? null : user.getAvatarUrl());

    }

    public static DiscordUserDisplay getUserInfoNotStripped(Context e, long discordID) {
        return handleUser(e, discordID);
    }

    public static DiscordUserDisplay getUserInfoConsideringGuildOrNot(Context e, long discordID) {
        DiscordUserDisplay discordUserDisplay = handleUser(e, discordID);
        return new DiscordUserDisplay(cleanMarkdownCharacter(discordUserDisplay.getUsername()), discordUserDisplay.getUrlImage());
    }


    public static char getMessagePrefix(Context e) {
        return e.getPrefix();
    }

    public static String encodeUrl(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    public static String cleanMarkdownCharacter(String string) {
        return LinkUtils.markdownStripper.matcher(string).replaceAll("\\\\$1");
    }

    public static String markdownLessString(String string) {
        if (!string.contains("\\")) {
            return string;
        }
        return string.replaceAll("\\\\", "");

    }

    public static String markdownLessUserString(String string, long discordId, Context e) {
        if (!string.contains("\\")) {
            return string;
        }
        return getUserInfoNotStripped(e, discordId).getUsername();
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

    public static String getTimestamp(long ms) {
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
            String date = CommandUtil.getAmericanizedDate(offsetDateTime);
            String link = String.format("%s/library?from=%s&rangetype=1day", PrivacyUtils.getLastFmUser(lastfmId), day);
            return new GlobalStreakEntities.DateHolder(streakStart, date, link);

        }
    }
}
