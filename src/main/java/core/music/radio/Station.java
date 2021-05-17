package core.music.radio;

import core.Chuu;
import core.apis.spotify.SpotifyUtils;
import core.commands.Context;
import core.commands.utils.PrivacyUtils;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static core.commands.discovery.ReleasesEveryNoiseCommand.RELEASES_URL;

public enum Station {

    RANDOM(RandomRadioTrackContext.class, "Uses the random pool of urls as the source of music", List.of("rand", "r")) {
        @Override
        public <T extends RadioTrackContext> MessageEmbed.Field fieldFunction(T item, Context e) {
            RandomRadioTrackContext randomDetails = (RandomRadioTrackContext) item;
            String pstr = "";
            try {
                LastFMData lastFMData = Chuu.getDb().findLastFMData(randomDetails.getCurrentSourcer());
                String publicStr = PrivacyUtils.getPublicStr(lastFMData.getPrivacyMode(), lastFMData.getDiscordId(), lastFMData.getName(), e);
                pstr = "\nSubmitted by %s".formatted(publicStr);
            } catch (Exception ex) {
                //
            }
            return new MessageEmbed.Field("Random info", "[Source link](%s)%s".formatted(randomDetails.getUri(), pstr), false);
        }


    }, RELEASES(ReleaseRadioTrackContext.class, "A playlist of new releases of a given genre", List.of("release", "new", "discover")) {
        @Override
        public <T extends RadioTrackContext> MessageEmbed.Field fieldFunction(T item, Context e) {
            ReleaseRadioTrackContext context = (ReleaseRadioTrackContext) item;
            return new MessageEmbed.Field("%s releases".formatted(context.getGenre()), "Releases from the genre [%s](%s).\nBrowse all the genre release @ [everynoise.com](%s)".formatted(context.getGenre(), SpotifyUtils.getPlaylistLink(context.getGenreUri()), URLEncoder.encode(RELEASES_URL.formatted(context.getGenre()), StandardCharsets.UTF_8)), false);
        }


    }, GENRE(GenreRadioTrackContext.class, "Gives you a curated playlist for a given genre", List.of("genre", "everynoise", "every", "genre", "tags")) {
        @Override
        public <T extends RadioTrackContext> MessageEmbed.Field fieldFunction(T item, Context e) {
            GenreRadioTrackContext context = (GenreRadioTrackContext) item;
            return new MessageEmbed.Field("Exploring %s".formatted(context.getGenre()), "Browsing the genre [%s](%s).".formatted(context.getGenre(), SpotifyUtils.getPlaylistLink(context.getUri())), false);
        }


    }, CURATED(RadioTrackContext.class, null, false) {
        @Override
        public <T extends RadioTrackContext> MessageEmbed.Field fieldFunction(T item, Context e) {
            return null;
        }


    };

    private final Class<? extends RadioTrackContext> clazz;
    private final String description;
    private final boolean active;
    private List<String> aliases;

    Station(Class<? extends RadioTrackContext> clazz, String description, List<String> aliases) {
        this(clazz, description);
        this.aliases = aliases;
    }

    Station(Class<? extends RadioTrackContext> clazz, String description) {
        this(clazz, description, true);
    }

    Station(Class<? extends RadioTrackContext> clazz, String description, boolean active) {
        this.clazz = clazz;
        this.description = description;
        this.aliases = Collections.emptyList();
        this.active = active;

    }

    public static MessageEmbed.Field getField(RadioTrackContext t, Context e) {
        return EnumSet.allOf(Station.class).stream().filter(z -> z.clazz.equals(t.getClass())).map(z -> z.fieldFunction(t, e)).findFirst().orElse(null);
    }

    abstract <T extends RadioTrackContext> MessageEmbed.Field fieldFunction(T item, Context e);


    public Class<? extends RadioTrackContext> getClazz() {
        return clazz;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public List<String> getAliases() {
        return aliases;
    }
}
