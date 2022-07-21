package core.commands.artists;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.*;
import core.exceptions.LastFmException;
import core.imagerenderer.util.pie.DefaultList;
import core.imagerenderer.util.pie.IPieableList;
import core.imagerenderer.util.pie.OptionalPie;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.params.ChuuDataParams;
import core.services.validators.ArtistValidator;
import core.util.ServiceView;
import dao.entities.AlbumUserPlays;
import dao.entities.RemainingImagesMode;
import dao.entities.ScrobbledArtist;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchart.PieChart;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class GlobalFavesFromArtistCommand extends ConcurrentCommand<ArtistParameters> {

    private final IPieableList<AlbumUserPlays, ChuuDataParams> pie;

    public GlobalFavesFromArtistCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = true;
        new OptionalPie(this.getParser());
        this.pie = DefaultList.fillPie(AlbumUserPlays::getAlbum, AlbumUserPlays::getPlays);
    }

    public static <T extends ChuuDataParams> void sendArtistFaves(Context e, ScrobbledArtist who, String validArtist, String lastFmName, List<AlbumUserPlays> faves,
                                                                  String footerString,
                                                                  String headerString, String inWhere,
                                                                  String url,
                                                                  T params,
                                                                  IPieableList<AlbumUserPlays, T> pie,
                                                                  Consumer<BufferedImage> pieConsumer) {

        if (faves.isEmpty()) {
            e.sendMessage("Couldn't find any tracks of " + CommandUtil.escapeMarkdown(who.getArtist()) + " " + inWhere).queue();
            return;
        }

        String footer = "%s have listened to %d different %s songs!".formatted(footerString, faves.size(), who.getArtist());
        String title = String.format("%s top %s tracks", headerString, who.getArtist());
        RemainingImagesMode effectiveMode = CommandUtil.getEffectiveMode(params.getLastFMData().getRemainingImagesMode(), params);
        switch (effectiveMode) {
            case IMAGE, LIST -> {

                EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                        .setAuthor(title, PrivacyUtils.getLastFmArtistUserUrl(who.getArtist(), lastFmName), url)
                        .setThumbnail(CommandUtil.noImageUrl(who.getUrl()));

                if (!StringUtils.isBlank(faves.get(0).getAlbumUrl())) {
                    embedBuilder.setFooter(footer, faves.get(0).getAlbumUrl());
                } else {
                    embedBuilder.setFooter(footer);
                }

                Function<AlbumUserPlays, String> mapper = g -> ". **[" + CommandUtil.escapeMarkdown(g.getAlbum()) + "](" + LinkUtils.getLastFMArtistTrack(validArtist, g.getAlbum()) + ")** - " + g.getPlays() + " plays" +
                        "\n";

                new PaginatorBuilder<>(e, embedBuilder, faves).mapper(mapper).build().queue();
            }
            case PIE -> {
                PieChart pieChart = pie.doPie(params, faves);
                pieChart.setTitle(title);
                BufferedImage image = new PieDoer(footer, who.getUrl(), pieChart).fill();
                pieConsumer.accept(image);
            }
        }
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Favourite tracks from an artist on the bot";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("globalfavs", "globalfavourites", "globalfavorites", "gfavs");
    }

    @Override
    public String slashName() {
        return "favs";
    }

    @Override
    public String getName() {

        return "Favs in the bot";
    }

    @Override
    public void onCommand(Context e, @Nonnull ArtistParameters params) throws LastFmException {

        long userId = params.getLastFMData().getDiscordId();
        String artist = params.getArtist();

        ScrobbledArtist who = new ArtistValidator(db, lastFM, e)
                .validate(artist, !params.isNoredirect());
        String validArtist = who.getArtist();

        String lastFmName = params.getLastFMData().getName();


        List<AlbumUserPlays> songs = db.getGlboalTopArtistTracks(who.getArtistId(), Integer.MAX_VALUE);

        sendArtistFaves(e, who, validArtist, lastFmName, songs, e.getJDA().getSelfUser().getName() + "'s users", e.getJDA().getSelfUser().getName() + "'s", "in the bot!", e.getJDA().getSelfUser().getAvatarUrl(), params, pie, b -> sendImage(b, e));
    }
}
