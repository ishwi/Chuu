package core.commands.utils;

import core.Chuu;
import core.commands.CommandUtil;
import dao.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static dao.utils.LinkUtils.encodeUrl;

public class PrivacyUtils {
    public static TriFunction<MessageReceivedEvent, AtomicInteger, Predicate<Long>, Consumer<GlobalStreakEntities>> consumer = (e, c, p) -> (x) -> {
        PrivacyMode privacyMode = x.getPrivacyMode();
        if (p.test(x.getDiscordId())) {
            privacyMode = PrivacyMode.DISCORD_NAME;
        }

        int andIncrement = c.getAndIncrement();
        String dayNumberSuffix = CommandUtil.getDayNumberSuffix(andIncrement);
        switch (privacyMode) {

            case STRICT:
            case NORMAL:
                x.setCalculatedDisplayName(dayNumberSuffix + " **Private User #" + c.getAndIncrement() + "**");
                break;
            case DISCORD_NAME:
                x.setCalculatedDisplayName(dayNumberSuffix + " **" + CommandUtil.getUserInfoNotStripped(e, x.getDiscordId()).getUsername() + "**");
                break;
            case TAG:
                x.setCalculatedDisplayName(dayNumberSuffix + " **" + e.getJDA().retrieveUserById(x.getDiscordId()).complete().getAsTag() + "**");
                break;
            case LAST_NAME:
                x.setCalculatedDisplayName(dayNumberSuffix + " **" + x.getLastfmId() + " (last.fm)**");
                break;
        }

    };

    public static String getLastFmAlbumUserUrl(String artist, String album, String username) {
        return getLastFmUser(username) + "/library/music/" + encodeUrl(artist) + "/" + encodeUrl(album);

    }

    public static String getLastFmArtistUserUrl(String artist, String username) {
        return getLastFmUser(username) + "/library/music/" + encodeUrl(artist);
    }

    public static String getLastFmUser(String username) {
        return "https://www.last.fm/user/" + encodeUrl(Chuu.getLastFmId(username));
    }

    public static String toString(LbEntry entry) {

        return entry.toStringWildcard().replace(LbEntry.WILDCARD, getLastFmUser(entry.getLastFmId()));

    }


    public static String getUrlTitle(ReturnNowPlaying returnNowPlaying) {
        String templated;
        if (returnNowPlaying instanceof ReturnNowPlayingAlbum) {
            ReturnNowPlayingAlbum p = (ReturnNowPlayingAlbum) returnNowPlaying;
            templated = getLastFmAlbumUserUrl(p.getArtist(), p.getAlbum(), p.getLastFMId());
        } else if (returnNowPlaying instanceof GlobalReturnNowPlayingAlbum) {
            GlobalReturnNowPlayingAlbum p = (GlobalReturnNowPlayingAlbum) returnNowPlaying;
            templated = getLastFmAlbumUserUrl(p.getArtist(), p.getAlbum(), p.getLastFMId());
        } else {
            templated = getLastFmArtistUserUrl(returnNowPlaying.getArtist(), returnNowPlaying.getLastFMId());
        }
        return templated;
    }
}


