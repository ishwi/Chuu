package core.commands.artists;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.imagerenderer.util.pie.DefaultList;
import core.imagerenderer.util.pie.IPieableList;
import core.imagerenderer.util.pie.OptionalPie;
import core.imagerenderer.util.pie.PieSetUp;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.params.ChuuDataParams;
import dao.ServiceView;
import dao.entities.AlbumUserPlays;
import dao.entities.RemainingImagesMode;
import dao.entities.ScrobbledArtist;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchart.PieChart;

import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GlobalFavesFromArtistCommand extends ConcurrentCommand<ArtistParameters> {

    private final DiscogsApi discogs;
    private final Spotify spotify;
    private final IPieableList<AlbumUserPlays, ChuuDataParams> pie;

    public GlobalFavesFromArtistCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = true;
        new OptionalPie(this.getParser());
        this.discogs = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
        this.pie = DefaultList.fillPie(AlbumUserPlays::getAlbum, AlbumUserPlays::getPlays);
    }

    public static <T extends ChuuDataParams> void sendArtistFaves(Context e, ScrobbledArtist who, String validArtist, String lastFmName, List<AlbumUserPlays> songs, String userString,
                                                                  String inWhere,
                                                                  String url,
                                                                  T params,
                                                                  IPieableList<AlbumUserPlays, T> pie,
                                                                  Consumer<BufferedImage> pieConsumer) {

        if (songs.isEmpty()) {
            e.sendMessage("Couldn't find any tracks of " + CommandUtil.escapeMarkdown(who.getArtist()) + " " + inWhere).queue();
            return;
        }

        String footer = "%s users have listened to %d different %s songs!".formatted(userString, songs.size(), who.getArtist());
        String title = String.format("%s's top %s tracks", userString, who.getArtist());
        RemainingImagesMode effectiveMode = CommandUtil.getEffectiveMode(params.getLastFMData().getRemainingImagesMode(), params);
        switch (effectiveMode) {
            case IMAGE, LIST -> {
                StringBuilder a = new StringBuilder();
                List<String> s = songs.stream().map(g -> ". **[" + CommandUtil.escapeMarkdown(g.getAlbum()) + "](" + LinkUtils.getLastFMArtistTrack(validArtist, g.getAlbum()) + ")** - " + g.getPlays() + " plays" +
                                                         "\n").toList();
                for (int i = 0; i < s.size() && i < 10; i++) {
                    String sb = s.get(i);
                    a.append(i + 1).append(sb);
                }

                EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                        .setDescription(a)
                        .setAuthor(title, PrivacyUtils.getLastFmArtistUserUrl(who.getArtist(), lastFmName), url)
                        .setThumbnail(CommandUtil.noImageUrl(who.getUrl()));


                if (!StringUtils.isBlank(songs.get(0).getAlbumUrl())) {
                    embedBuilder.setFooter(footer, songs.get(0).getAlbumUrl());
                } else {
                    embedBuilder.setFooter(footer);
                }

                e.sendMessage(embedBuilder.build()).queue(mes ->
                        new Reactionary<>(s, mes, embedBuilder));
            }
            case PIE -> {
                PieChart pieChart = pie.doPie(params, songs);
                pieChart.setTitle(title);
                BufferedImage image = new PieSetUp(footer, who.getUrl(), pieChart).setUp();
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
    protected void onCommand(Context e, @NotNull ArtistParameters params) throws LastFmException {

        long userId = params.getLastFMData().getDiscordId();
        String artist = params.getArtist();

        ScrobbledArtist who = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(db, who, lastFM, discogs, spotify);
        String validArtist = who.getArtist();

        String lastFmName = params.getLastFMData().getName();


        List<AlbumUserPlays> songs = db.getGlboalTopArtistTracks(who.getArtistId(), Integer.MAX_VALUE);

        sendArtistFaves(e, who, validArtist, lastFmName, songs, e.getJDA().getSelfUser().getName(), "in the bot!", e.getJDA().getSelfUser().getAvatarUrl(), params, pie, b -> sendImage(b, e));
    }
}
