package core.parsers.interactions;

import core.apis.last.ConcurrentLastFM;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.ChartParserAux;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.exceptions.InvalidDateException;
import core.parsers.explanation.*;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;
import core.parsers.utils.CustomTimeFrame;
import core.services.NPService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InteractionAux {

    private InteractionAux() {


    }

    public static TimeFrameEnum parseTimeFrame(CommandInteraction e, TimeFrameEnum fallback) {
        return Optional.ofNullable(e.getOption(TimeframeExplanation.NAME)).map(OptionMapping::getAsString).map(TimeFrameEnum::get).orElse(fallback);
    }

    public static NaturalTimeFrameEnum parseNaturalTimeFrame(CommandInteraction e, NaturalTimeFrameEnum fallback) {
        return Optional.ofNullable(e.getOption(NaturalTimeframeExplanation.NAME)).map(OptionMapping::getAsString).map(NaturalTimeFrameEnum::get).orElse(fallback);
    }

    public static User parseUser(CommandInteraction e) {
        if (e instanceof UserContextInteraction uci) {
            return uci.getTarget();
        }
        return Optional.ofNullable(e.getOption(StrictUserExplanation.NAME)).map(OptionMapping::getAsUser).orElse(e.getUser());
    }

    public static @Nullable
    String parseUrl(CommandInteraction e) {
        return Optional.ofNullable(e.getOption(UrlExplanation.NAME)).map(OptionMapping::getAsString).orElse(null);
    }

    public static String parseSize(CommandInteraction e) {
        return Optional.ofNullable(e.getOption(ChartSizeExplanation.NAME)).map(OptionMapping::getAsString).orElse("5x5");
    }

    public static @Nullable
    Point parseSize(CommandInteraction e, Callback errorMessage) {
        int x = 5;
        int y = 5;
        try {
            return ChartParserAux.processString(parseSize(e));
        } catch (InvalidChartValuesException ex) {
            errorMessage.execute();
            return null;
        }
    }


    public static @Nullable
    ArtistAlbum parseAlbum(CommandInteraction e, Callback errorMessage) {
        return parseCommonArtistAlbum(errorMessage, e, AlbumExplanation.NAME);
    }

    public static Explanation required(Explanation explanation) {
        Interactible intercepted = explanation.explanation();
        List<OptionData> optionData = explanation.explanation().options();
        optionData.forEach(t -> t.setRequired(true));

        return () -> new ExplanationLine(intercepted.header(), intercepted.usage(), optionData);
    }

    @org.jetbrains.annotations.Nullable
    public static InteractionAux.ArtistAlbum parseCommonArtistAlbum(Callback errorCallback, CommandInteraction e, String explanation) {
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

    public static @Nullable
    ArtistAlbum parseSong(CommandInteraction e, Callback errorMessage) {
        return parseCommonArtistAlbum(errorMessage, e, TrackExplanation.NAME);

    }

    public static <T> T processArtist(InteracionReceived<? extends CommandInteraction> ctx, ConcurrentLastFM lastFM, User author, User oneUser, LastFMData data, Function<User, Optional<LastFMData>> getter, boolean forComparison, boolean isAllowUnaothorizedUsers, BiFunction<NowPlayingArtist, LastFMData, T> npFound, BiFunction<String, LastFMData, T> notFound) throws LastFmException, InstanceNotFoundException {
        CommandInteraction e = ctx.e();
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

    public static <T> T processAlbum(@Nonnull ArtistAlbum album, ConcurrentLastFM lastFM, LastFMData callerData, boolean forComparison, User author, User caller, Function<User, Optional<LastFMData>> getter, BiFunction<NowPlayingArtist, LastFMData, T> npFound, BiFunction<String[], LastFMData, T> notFound) throws LastFmException, InstanceNotFoundException {

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

    public static @Nullable
    Year parseYear(CommandInteraction e, Callback errorCallback) {
        Year year = Optional.ofNullable(e.getOption(YearExplanation.NAME)).map(OptionMapping::getAsLong).map(Long::intValue).map(Year::of).orElse(Year.now());
        if (Year.now().compareTo(year) < 0) {
            errorCallback.execute();
            return null;
        }
        return year;
    }

    public static CustomTimeFrame parseCustomTimeFrame(CommandInteraction e, TimeFrameEnum defaulted) throws InvalidDateException {
        OptionMapping option = e.getOption(TimeframeExplanation.NAME);
        if (option != null) {
            return CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.get(option.getAsString()));
        }
        String from = Optional.ofNullable(e.getOption("from")).map(OptionMapping::getAsString).orElse("");
        String to = Optional.ofNullable(e.getOption("to")).map(OptionMapping::getAsString).orElse("");
        String[] message;
        if (StringUtils.isBlank(to)) {
            message = new String[]{from};
        } else {
            message = new String[]{from, "-", to};
        }
        ChartParserAux chartParserAux = new ChartParserAux(message);
        return chartParserAux.parseCustomTimeFrame(defaulted);

    }

    public record ArtistAlbum(String artist, String album) {
        public boolean empty() {
            return artist == null || album == null;
        }
    }


}
