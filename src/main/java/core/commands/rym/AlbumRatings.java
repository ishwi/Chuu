package core.commands.rym;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.services.validators.AlbumValidator;
import core.services.validators.ArtistValidator;
import core.util.ServiceView;
import dao.entities.FullAlbumEntityExtended;
import dao.entities.Rating;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class AlbumRatings extends ConcurrentCommand<ArtistAlbumParameters> {

    public AlbumRatings(ServiceView dao) {
        super(dao);
    }

    @NotNull
    public static Function<Byte, String> getStartsFromScore() {
        return (score) -> {
            float number = score / 2f;
            String starts = "★".repeat((int) number);
            if (number % 1 != 0)
                starts += "☆";
            return starts;
        };
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.RYM;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {

        return new ArtistAlbumParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Album Ratings of users that were uploaded to RYM";
    }

    @Override
    public List<String> getAliases() {
        return List.of("rating", "rymal", "albumrating", "ral");
    }

    @Override
    public String getName() {
        return "Album Rating";
    }

    @Override
    public void onCommand(Context e, @NotNull ArtistAlbumParameters params) throws LastFmException {
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);

        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), !params.isNoredirect());

        ScrobbledAlbum validate = new AlbumValidator(db, lastFM)
                .validate(sA.getArtistId(), sA.getArtist(), params.getAlbum());

        String album = params.getAlbum();
        String artist = sA.getArtist();
        List<Rating> userRatings;
        Year releaseYear = null;
        if (e.isFromGuild()) {
            dao.entities.AlbumRatings userrat = db.getRatingsByName(e.getGuild().getIdLong(), album, sA.getArtistId());
            userRatings = userrat.userRatings();
            releaseYear = userrat.releaseYear();
        } else {
            userRatings = Optional.ofNullable(db.getUserAlbumRating(params.getLastFMData().getDiscordId(), validate.getAlbumId(), validate.getArtistId())).map(List::of).orElse(Collections.emptyList());
        }

        NumberFormat average = new DecimalFormat("#0.##");
        Function<Byte, String> starFormatter = getStartsFromScore();
        FullAlbumEntityExtended albumSummary = lastFM.getAlbumSummary(params.getLastFMData(), sA.getArtist(), album);
        String lastFmArtistAlbumUrl = LinkUtils.getLastFmArtistAlbumUrl(artist, album);
        List<String> stringList = userRatings.stream().filter(Rating::isSameGuild).map(x -> ". **[" +
                                                                                            getUserString(e, x.discordId()) +
                                                                                            "](" + lastFmArtistAlbumUrl +
                                                                                            ")** - " + starFormatter.apply(x.rating()) +
                                                                                            "\n").toList();
        if (stringList.isEmpty()) {
            sendMessageQueue(e, String.format("**%s** by **%s** was not rated by anyone.", album, artist));
            return;
        }


        String chuu = albumSummary.getAlbumUrl();
        List<Rating> servcerList = userRatings.stream().filter(Rating::isSameGuild).toList();
        String serverName = e.getGuild().getName();
        String botName = e.getJDA().getSelfUser().getName();
        String name = String.format("%s Average: **%s** | Ratings: **%d**", serverName, average.format(servcerList.stream().mapToDouble(rating -> rating.rating() / 2f).average().orElse(0)), servcerList.size());
        String global = String.format("%s Average: **%s** | Ratings: **%d**", botName, average.format(userRatings.stream().mapToDouble(rating -> rating.rating() / 2f).average().orElse(0)), userRatings.size());

        embedBuilder.setTitle(String.format("%s - %s Ratings in %s", albumSummary.getArtist(), albumSummary.getAlbum(), serverName), lastFmArtistAlbumUrl)

                .setFooter(String.format("%s%s has been rated by %d %s.", albumSummary.getAlbum(),
                        releaseYear != null ? " (" + releaseYear + ")" : ""
                        , userRatings.size(),
                        CommandUtil.singlePlural(userRatings.size(), "person", "people")))
                .setThumbnail(chuu);

        new PaginatorBuilder<>(e, embedBuilder, stringList).extraText(integer -> name + "\n" + global).build().queue();


    }
}
