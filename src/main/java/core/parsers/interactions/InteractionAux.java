package core.parsers.interactions;

import core.apis.last.ConcurrentLastFM;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.ChartParserAux;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.explanation.*;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;
import core.services.NPService;
import dao.entities.Callback;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InteractionAux {
    static {

    }

    private InteractionAux() {


    }

    public static TimeFrameEnum parseTimeFrame(SlashCommandEvent e, TimeFrameEnum fallback) {
        return Optional.ofNullable(e.getOption(TimeframeExplanation.NAME)).map(SlashCommandEvent.OptionData::getAsString).map(TimeFrameEnum::getFromComplete).orElse(fallback);
    }

    public static User parseUser(SlashCommandEvent e) {
        return Optional.ofNullable(e.getOption(StrictUserExplanation.NAME)).map(SlashCommandEvent.OptionData::getAsUser).orElse(e.getUser());
    }

    public static @Nullable String parseUrl(SlashCommandEvent e) {
        return Optional.ofNullable(e.getOption(UrlExplanation.NAME)).map(SlashCommandEvent.OptionData::getAsString).orElse(null);
    }

    public static String parseSize(SlashCommandEvent e) {
        return Optional.ofNullable(e.getOption(ChartSizeExplanation.NAME)).map(SlashCommandEvent.OptionData::getAsString).orElse("5x5");
    }

    public static @Nullable Point parseSize(SlashCommandEvent e, Callback errorMessage) {
        int x = 5;
        int y = 5;
        try {
            return ChartParserAux.processString(parseSize(e));
        } catch (InvalidChartValuesException ex) {
            errorMessage.executeCallback();
            return null;
        }
    }


    public static @Nullable ArtistAlbum parseAlbum(SlashCommandEvent e, Callback errorMessage) {
        return parseCommon(errorMessage, e, AlbumExplanation.NAME);
    }

    public static Explanation required(Explanation explanation) {
        Interactible explanation1 = explanation.explanation();
        return () -> new ExplanationLine(explanation1.header(), explanation1.usage(), explanation.explanation().optionData().setRequired(true));
    }

    @org.jetbrains.annotations.Nullable
    public static InteractionAux.ArtistAlbum parseCommon(Callback errorMessage, SlashCommandEvent e, String explanation) {
        var artist = e.getOption(ArtistExplanation.NAME);
        var album = e.getOption(explanation);

        if ((artist == null && album != null) || (album == null && artist != null)) {
            errorMessage.executeCallback();
            return null;
        }
        String artist1 = Optional.ofNullable(artist).map(SlashCommandEvent.OptionData::getAsString).orElse(null);
        String album1 = Optional.ofNullable(album).map(SlashCommandEvent.OptionData::getAsString).orElse(null);
        return new ArtistAlbum(artist1, album1);
    }

    public static @Nullable ArtistAlbum parseSong(SlashCommandEvent e, Callback errorMessage) {
        return parseCommon(errorMessage, e, TrackExplanation.NAME);

    }

    public static <T> T processArtist(ContextSlashReceived ctx, ConcurrentLastFM lastFM, User author, User oneUser, LastFMData data, Function<User, Optional<LastFMData>> getter, boolean forComparison, boolean isAllowUnaothorizedUsers,
                                      BiFunction<NowPlayingArtist, LastFMData, T> npFound,
                                      BiFunction<String, LastFMData, T> notFound
    ) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        var artist = e.getOption(ArtistExplanation.NAME);


        if (artist == null) {
            NowPlayingArtist np;
            if (isAllowUnaothorizedUsers && data.getName() == null) {
                throw new InstanceNotFoundException(oneUser.getIdLong());
            }
            try {
                if (forComparison && author.getIdLong() != oneUser.getIdLong()) {
                    LastFMData lastfmFromID = getter.apply(author).orElseThrow(() -> new InstanceNotFoundException(author.getIdLong()));
                    np = new NPService(lastFM, lastfmFromID).getNowPlaying();
                } else {
                    np = new NPService(lastFM, data).getNowPlaying();
                }
            } catch (InstanceNotFoundException ex) {
                np = new NPService(lastFM, data).getNowPlaying();
            }
            return npFound.apply(np, data);
        } else {
            return notFound.apply(artist.getAsString(), data);
        }
    }

    public static <T> T processAlbum(@NotNull ArtistAlbum album, ConcurrentLastFM lastFM,
                                     LastFMData callerData, boolean forComparison,
                                     User author,
                                     User caller,
                                     Function<User, Optional<LastFMData>> getter,
                                     BiFunction<NowPlayingArtist, LastFMData, T> npFound,
                                     BiFunction<String[], LastFMData, T> notFound
    ) throws LastFmException, InstanceNotFoundException {

        if (album.empty()) {
            NowPlayingArtist np;
            try {
                if (forComparison && author.getIdLong() != caller.getIdLong()) {
                    LastFMData lastfmFromID = getter.apply(author).orElseThrow(() -> new InstanceNotFoundException(author.getIdLong()));
                    np = new NPService(lastFM, lastfmFromID).getNowPlaying();
                } else {
                    np = new NPService(lastFM, callerData).getNowPlaying();
                }
            } catch (InstanceNotFoundException ex) {
                np = new NPService(lastFM, callerData).getNowPlaying();
            }

            return npFound.apply(np, callerData);

        } else {
            return notFound.apply(new String[]{album.artist, " - ", album.album}, callerData);
        }
    }

    public record ArtistAlbum(String artist, String album) {
        public boolean empty() {
            return artist == null || album == null;
        }
    }


}
