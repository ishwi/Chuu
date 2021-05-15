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
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InteractionAux {

    private InteractionAux() {


    }

    public static TimeFrameEnum parseTimeFrame(SlashCommandEvent e, TimeFrameEnum fallback) {
        return Optional.ofNullable(e.getOption(TimeframeExplanation.NAME)).map(OptionMapping::getAsString).map(TimeFrameEnum::getFromComplete).orElse(fallback);
    }

    public static NaturalTimeFrameEnum parseNaturalTimeFrame(SlashCommandEvent e, NaturalTimeFrameEnum fallback) {
        return Optional.ofNullable(e.getOption(NaturalTimeframeExplanation.NAME)).map(OptionMapping::getAsString).map(NaturalTimeFrameEnum::getFromComplete).orElse(fallback);
    }

    public static User parseUser(SlashCommandEvent e) {
        return Optional.ofNullable(e.getOption(StrictUserExplanation.NAME)).map(OptionMapping::getAsUser).orElse(e.getUser());
    }

    public static @Nullable String parseUrl(SlashCommandEvent e) {
        return Optional.ofNullable(e.getOption(UrlExplanation.NAME)).map(OptionMapping::getAsString).orElse(null);
    }

    public static String parseSize(SlashCommandEvent e) {
        return Optional.ofNullable(e.getOption(ChartSizeExplanation.NAME)).map(OptionMapping::getAsString).orElse("5x5");
    }

    public static @Nullable Point parseSize(SlashCommandEvent e, Callback errorMessage) {
        int x = 5;
        int y = 5;
        try {
            return ChartParserAux.processString(parseSize(e));
        } catch (InvalidChartValuesException ex) {
            errorMessage.execute();
            return null;
        }
    }


    public static @Nullable ArtistAlbum parseAlbum(SlashCommandEvent e, Callback errorMessage) {
        return parseCommonArtistAlbum(errorMessage, e, AlbumExplanation.NAME);
    }

    public static Explanation required(Explanation explanation) {
        Interactible intercepted = explanation.explanation();
        List<OptionData> optionData = explanation.explanation().options();
        optionData.forEach(t -> t.setRequired(true));

        return () -> new ExplanationLine(intercepted.header(), intercepted.usage(), optionData);
    }

    @org.jetbrains.annotations.Nullable
    public static InteractionAux.ArtistAlbum parseCommonArtistAlbum(Callback errorCallback, SlashCommandEvent e, String explanation) {
        var artist = e.getOption(ArtistExplanation.NAME);
        var album = e.getOption(explanation);

        if ((artist == null && album != null) || (album == null && artist != null)) {
            errorCallback.execute();
            return null;
        }
        String artist1 = Optional.ofNullable(artist).map(OptionMapping::getAsString).orElse(null);
        String album1 = Optional.ofNullable(album).map(OptionMapping::getAsString).orElse(null);
        return new ArtistAlbum(artist1, album1);
    }

    public static @Nullable ArtistAlbum parseSong(SlashCommandEvent e, Callback errorMessage) {
        return parseCommonArtistAlbum(errorMessage, e, TrackExplanation.NAME);

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

    public static @Nullable Year parseYear(SlashCommandEvent e, Callback errorCallback) {
        Year year = Optional.ofNullable(e.getOption(YearExplanation.NAME)).map(OptionMapping::getAsLong).map(Long::intValue).map(Year::of).orElse(Year.now());
        if (Year.now().compareTo(year) < 0) {
            errorCallback.execute();
            return null;
        }
        return year;
    }

    public record ArtistAlbum(String artist, String album) {
        public boolean empty() {
            return artist == null || album == null;
        }
    }


}
