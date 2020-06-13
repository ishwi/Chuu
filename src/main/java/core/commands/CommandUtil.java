package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import core.exceptions.DiscogsServiceException;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class CommandUtil {

    private static final Pattern markdownStripper = Pattern.compile("((?<!\\\\)[*_~|`>\\[()\\]])");
    static final Random rand = new Random();

    private CommandUtil() {
    }

    static String noImageUrl(String artist) {
        return artist == null || artist
                .isEmpty() ? "https://lastfm-img2.akamaized.net/i/u/174s/4128a6eb29f94943c9d206c08e625904" : artist;
    }

    public static Color randomColor() {
        double r = rand.nextFloat() / 2f + 0.5;
        double g = rand.nextFloat() / 2f + 0.5;
        double b = rand.nextFloat() / 2f + 0.5;
        return new Color((float) r, (float) g, (float) b);
    }


    static BufferedImage getLogo(ChuuService dao, MessageReceivedEvent e) {
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
                newUrl = spotify.getArtistUrlImage(scrobbledArtist.getArtist());
                if (newUrl.isBlank()) {
                    scrobbledArtist.setUrl("");
                    scrobbledArtist.setUpdateBit(false);
                    dao.upsertArtistSad(scrobbledArtist);
                } else {
                    dao.upsertSpotify(newUrl, scrobbledArtist.getArtistId());
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
            }
            if (scrobbledArtist.getUrl() == null || scrobbledArtist.getUrl().isBlank()) {
                scrobbledArtist.setUrl(null);
            }
        }
    }

    static ScrobbledArtist onlyCorrection(ChuuService dao, String artist, ConcurrentLastFM lastFM, boolean doCorrection) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        validate(dao, scrobbledArtist, lastFM, null, null, false, doCorrection);
        return scrobbledArtist;
    }


    public static String getLastFmArtistUrl(String artist) {
        return "https://www.last.fm/music/" + encodeUrl(artist);
    }

    public static String getLastFmArtistAlbumUrl(String artist, String album) {
        return "https://www.last.fm/music/" + encodeUrl(artist) + "/" + encodeUrl(album);
    }

    public static String getLastFmTagUrl(String tag) {
        return "https://www.last.fm/tag/" + encodeUrl(tag);
    }

    public static String getLastFmArtistUserUrl(String artist, String username) {
        return getLastFmUser(username) + "/library/music/" + encodeUrl(artist);
    }

    public static String getLastFmUser(String username) {
        return "https://www.last.fm/user/" + encodeUrl(username);
    }

    public static String singlePlural(int count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }


    static Long getGuildIdConsideringPrivateChannel(MessageReceivedEvent e) {
        if (e.getChannelType().isGuild())
            return (e.getGuild().getIdLong());
        else {
            User user;
            if ((user = e.getJDA().getUserById(e.getAuthor().getIdLong())) == null) {
                return null;
            } else {
                List<Guild> mutualGuilds = user.getMutualGuilds();
                if (mutualGuilds.isEmpty()) {
                    return null;
                } else {

                    return mutualGuilds.get(0).getIdLong();

                }
            }
        }
    }

    static String getGlobalUsername(JDA jda, long discordID) {
        return CommandUtil.cleanMarkdownCharacter(jda.retrieveUserById(discordID).complete().getName());
    }

    // ugh
    private static DiscordUserDisplay handleUser(MessageReceivedEvent e, long discordID) {
        User user;
        String username;
        if (e.isFromGuild()) {
            Member whoD = e.getGuild().getMemberById(discordID);
            if (whoD == null) {
                user = e.getJDA().retrieveUserById(discordID).complete();
                username = user.getName();
            } else {
                username = whoD.getEffectiveName();
                user = whoD.getUser();
            }
        } else {
            user = e.getJDA().retrieveUserById(discordID).complete();
            username = user.getName();

        }
        return new DiscordUserDisplay((username), user.getAvatarUrl() == null || user.getAvatarUrl().isBlank() ? null : user.getAvatarUrl());

    }

    public static DiscordUserDisplay getUserInfoNotStripped(MessageReceivedEvent e, long discordID) {
        return handleUser(e, discordID);
    }

    public static DiscordUserDisplay getUserInfoConsideringGuildOrNot(MessageReceivedEvent e, long discordID) {
        DiscordUserDisplay discordUserDisplay = handleUser(e, discordID);
        return new DiscordUserDisplay(cleanMarkdownCharacter(discordUserDisplay.getUsername()), discordUserDisplay.getUrlImage());
    }

    public static String sanitizeUserString(String message) {
        return message.replace("@", "@\u200E");
    }

    public static String getLastFMArtistTrack(String artist, String track) {
        return getLastFmArtistUrl(artist) + "/_/" + encodeUrl(track);

    }


    public static char getMessagePrefix(MessageReceivedEvent e) {
        return e.getMessage().getContentRaw().charAt(0);
    }

    public static String encodeUrl(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    public static String cleanMarkdownCharacter(String string) {
        return markdownStripper.matcher(string).replaceAll("\\\\$1");
    }

    public static String markdownLessString(String string) {
        if (!string.contains("\\")) {
            return string;
        }
        return string.replaceAll("\\\\", "");

    }

    public static String markdownLessUserString(String string, long discordId, MessageReceivedEvent e) {
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

    static String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static RemainingImagesMode getEffectiveMode(RemainingImagesMode remainingImagesMode, CommandParameters chartParameters) {
        boolean pie = chartParameters.hasOptional("--pie");
        boolean list = chartParameters.hasOptional("--list");
        if ((remainingImagesMode.equals(RemainingImagesMode.LIST) && !list && !pie) || (!remainingImagesMode.equals(RemainingImagesMode.LIST) && list)) {
            return RemainingImagesMode.LIST;
        } else if (remainingImagesMode.equals(RemainingImagesMode.PIE) && !pie || !remainingImagesMode.equals(RemainingImagesMode.PIE) && pie) {
            return RemainingImagesMode.PIE;
        } else {
            return RemainingImagesMode.IMAGE;
        }
    }
}