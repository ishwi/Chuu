package core.commands.utils;

import core.Chuu;
import core.commands.Context;
import dao.entities.*;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static dao.utils.LinkUtils.encodeUrl;

public class PrivacyUtils {
    public static final TriFunction<Context, AtomicInteger, Predicate<Long>, Consumer<GlobalStreakEntities>> consumer = (e, c, p) -> (x) -> {
        PrivacyMode privacyMode = x.getPrivacyMode();
        if (p.test(x.getDiscordId())) {
            privacyMode = PrivacyMode.DISCORD_NAME;
        }

        int andIncrement = c.getAndIncrement();
        String dayNumberSuffix = CommandUtil.getDayNumberSuffix(andIncrement);
        switch (privacyMode) {
            case STRICT, NORMAL -> x.setCalculatedDisplayName(dayNumberSuffix + " **Private User #" + c.getAndIncrement() + "**");
            case DISCORD_NAME -> x.setCalculatedDisplayName(dayNumberSuffix + " **" + CommandUtil.getUserInfoUnescaped(e, x.getDiscordId()).getUsername() + "**");
            case TAG -> x.setCalculatedDisplayName(dayNumberSuffix + " **" + e.getJDA().retrieveUserById(x.getDiscordId(), false).complete().getAsTag() + "**");
            case LAST_NAME -> x.setCalculatedDisplayName(dayNumberSuffix + " **" + x.getLastfmId() + " (last.fm)**");
        }

    };

    public static String getLastFmAlbumUserUrl(String artist, String album, String username) {
        return getLastFmUser(username) + "/library/music/" + encodeUrl(artist) + "/" + encodeUrl(album);

    }

    public static String getLastFmArtistUserUrl(String artist, String username) {
        return getLastFmUser(username) + "/library/music/" + encodeUrl(artist);
    }

    public static String getLastFmArtistTrackUserUrl(String artist, String track, String username) {
        return getLastFmUser(username) + "/library/music/" + encodeUrl(artist) + "/_/" + encodeUrl(track);
    }

    public static String getLastFmGenreUserUrl(String genre, String username) {
        return getLastFmUser(username) + "/tags/" + encodeUrl(genre);
    }

    public static String getLastFmUser(String username) {
        return "https://www.last.fm/user/" + encodeUrl(Chuu.getLastFmId(username));
    }

    public static String toString(LbEntry entry) {

        return entry.toStringWildcard().replace(LbEntry.WILDCARD, getLastFmUser(entry.getLastFmId()));

    }


    public static String getUrlTitle(ReturnNowPlaying returnNowPlaying) {
        if (returnNowPlaying instanceof ReturnNowPlayingAlbum p) {
            return getLastFmAlbumUserUrl(p.getArtist(), p.getAlbum(), p.getLastFMId());
        } else if (returnNowPlaying instanceof GlobalReturnNowPlayingAlbum p) {
            return getLastFmAlbumUserUrl(p.getArtist(), p.getAlbum(), p.getLastFMId());
        } else if (returnNowPlaying instanceof TagPlaying p) {
            return getLastFmGenreUserUrl(p.getArtist(), p.getLastFMId());
        } else {
            return getLastFmArtistUserUrl(returnNowPlaying.getArtist(), returnNowPlaying.getLastFMId() == null ? Chuu.DEFAULT_LASTFM_ID : returnNowPlaying.getLastFMId());
        }
    }

    public static PrivateString getPublicString(PrivacyMode privacyMode, long discordId, String lastfmId, AtomicInteger atomicInteger, Context e, Set<Long> showableUsers) {
        if (showableUsers.contains(discordId)) {
            privacyMode = PrivacyMode.DISCORD_NAME;
        }
        return switch (privacyMode) {
            case STRICT, NORMAL -> new PrivateString("Private User #" + atomicInteger.getAndIncrement(), Chuu.DEFAULT_LASTFM_ID);
            case DISCORD_NAME -> new PrivateString(CommandUtil.getUserInfoUnescaped(e, discordId).getUsername(), Chuu.getLastFmId(lastfmId));
            case TAG -> new PrivateString(Chuu.getShardManager().retrieveUserById(discordId).complete().getAsTag(), Chuu.getLastFmId(lastfmId));
            case LAST_NAME -> new PrivateString(lastfmId + " (last.fm)", Chuu.getLastFmId(lastfmId));
        };
    }

    public static String getPublicStr(PrivacyMode privacyMode, long discordId, String lastfmId, Context e) {
        return switch (privacyMode) {
            case DISCORD_NAME -> CommandUtil.getUserInfoUnescaped(e, discordId).getUsername();
            case TAG -> Chuu.getShardManager().retrieveUserById(discordId).complete().getAsTag();
            case LAST_NAME -> lastfmId + " (last.fm)";
            default -> "Unknown";
        };
    }

    public record PrivateString(String discordName, String lastfmName) {
    }

}


