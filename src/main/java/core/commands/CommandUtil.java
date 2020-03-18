package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import core.exceptions.DiscogsServiceException;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.ScrobbledArtist;
import dao.entities.UpdaterStatus;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

public class CommandUtil {
    static String noImageUrl(String artist) {
        return artist == null || artist
                .isEmpty() ? "https://lastfm-img2.akamaized.net/i/u/174s/4128a6eb29f94943c9d206c08e625904" : artist;
    }

    public static Color randomColor() {
        Random rand = new Random();
        double r = rand.nextFloat() / 2f + 0.5;
        double g = rand.nextFloat() / 2f + 0.5;
        double b = rand.nextFloat() / 2f + 0.5;
        return new Color((float) r, (float) g, (float) b);
    }

//	static CompletableFuture<String> getDiscogsUrlAync(DiscogsApi discogsApi, String artist, DaoImplementation dao) {
//		return CompletableFuture.supplyAsync(() -> updateUrl(discogsApi, artist, dao));
//	}

    static BufferedImage getLogo(ChuuService dao, MessageReceivedEvent e) {
        try (InputStream stream = dao.findLogo(e.getGuild().getIdLong())) {
            if (stream != null)
                return ImageIO.read(stream);
        } catch (IOException ex) {
            return null;
        }
        return null;
    }


    public static String updateUrl(DiscogsApi discogsApi, ScrobbledArtist scrobbledArtist, ChuuService dao, Spotify spotify) {
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


        String dbCorrection = dao.findCorrection(scrobbledArtist.getArtist());
        if (dbCorrection != null) {
            scrobbledArtist.setArtist(dbCorrection);
        }
        boolean existed;
        //Find by id//
        // Doesnt exist? -> search for lastfm correction
        UpdaterStatus status = null;
        try {
            status = dao.getUpdaterStatusByName(scrobbledArtist.getArtist());
            scrobbledArtist.setArtistId(status.getArtistId());
            existed = true;
        } catch (InstanceNotFoundException e) {
            //Artist doesnt exists
            existed = false;
            String correction = lastFM.getCorrection(scrobbledArtist.getArtist());
            //Has a correction
            long byNameConsequent = dao.findByNameConsequent(correction);
            scrobbledArtist.setArtistId(byNameConsequent);

            if (!scrobbledArtist.getArtist().equalsIgnoreCase(correction)) {
                dao.insertCorrection(byNameConsequent, scrobbledArtist.getArtist());
                scrobbledArtist.setArtist(correction);
            }
        }
        if (doUrlCheck) {
            if (!existed || (status.getArtistUrl() == null))
                scrobbledArtist.setUrl(CommandUtil.updateUrl(discogsApi, scrobbledArtist, dao, spotify));
            else {
                scrobbledArtist.setUrl(status.getArtistUrl());
            }
        }
    }

    static ScrobbledArtist onlyCorrection(ChuuService dao, String artist, ConcurrentLastFM lastFM) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        validate(dao, scrobbledArtist, lastFM, null, null, false);
        return scrobbledArtist;
    }


    public static String getLastFmArtistUrl(String artist) {
        return "https://www.last.fm/music/" + artist.replaceAll(" ", "+").replaceAll("[)]", "%29");

    }

    public static String getLastFmArtistAlbumUrl(String artist, String album) {
        return "https://www.last.fm/music/" + artist.replaceAll(" ", "+").replaceAll("[)]", "%29") + "/" + album.replaceAll(" ", "+").replaceAll("[)]", "%29");

    }

    public static String getLastFmTagUrl(String tag) {
        return "https://www.last.fm/tag/" + tag.replaceAll(" ", "+").replaceAll("[)]", "%29");

    }

    public static String getLastFmArtistUserUrl(String artist, String username) {
        return getLastFmUser(username) + "/library/music/" + artist.replaceAll(" ", "+")
                .replaceAll("[)]", "%29");
    }

    public static String getLastFmUser(String username) {
        return "https://www.last.fm/user/" + username;
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
        return jda.retrieveUserById(discordID).complete().getName();
    }

    static DiscordUserDisplay getUserInfoConsideringGuildOrNot(MessageReceivedEvent e, long discordID) {
        String username;
        User user;
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
        return new DiscordUserDisplay(MarkdownSanitizer.sanitize(username), user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
    }

    public static String sanitizeUserString(String message) {
        return message.replaceAll("@", "@\u200E");
    }

    public static String getLastFMArtistTrack(String artist, String track) {
        return getLastFmArtistUrl(artist) + "/_/" + track.replaceAll(" ", "+")
                .replaceAll("[)]", "%29");
    }

    public static char getMessagePrefix(MessageReceivedEvent e) {
        return e.getMessage().getContentRaw().charAt(0);
    }

}