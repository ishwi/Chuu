package core.commands.artists;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.*;
import core.exceptions.LastFmException;
import core.imagerenderer.util.pie.DefaultList;
import core.imagerenderer.util.pie.IPieableList;
import core.imagerenderer.util.pie.OptionalPie;
import core.imagerenderer.util.pie.PieSetUp;
import core.parsers.ArtistTimeFrameParser;
import core.parsers.Parser;
import core.parsers.params.ArtistTimeFrameParameters;
import core.parsers.params.CommandParameters;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.*;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchart.PieChart;

import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class FavesFromArtistCommand extends ConcurrentCommand<ArtistTimeFrameParameters> {

    private final DiscogsApi discogs;
    private final Spotify spotify;
    private final IPieableList<Track, CommandParameters> pie;

    public FavesFromArtistCommand(ServiceView dao) {
        super(dao);
        this.pie = DefaultList.fillPie(Track::getPie, Track::getPlays);
        new OptionalPie(this.getParser());
        respondInPrivate = true;
        this.discogs = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistTimeFrameParameters> initParser() {
        return new ArtistTimeFrameParser(db, lastFM, true);
    }

    @Override
    public String getDescription() {
        return "Your favourite tracks from an artist";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("favs", "favourites", "favorites");
    }

    @Override
    public String getName() {

        return "Fav tracks";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistTimeFrameParameters params) throws LastFmException {

        long userId = params.getLastFMData().getDiscordId();
        TimeFrameEnum timeframew = params.getTimeFrame();

        ScrobbledArtist who = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), !params.isNoredirect());
        String artist = who.getArtist();
        List<Track> ai;
        String lastFmName = params.getLastFMData().getName();

        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, userId);
        String userString = uInfo.getUsername();
        if (timeframew.equals(TimeFrameEnum.ALL)) {
            ai = db.getTopArtistTracks(lastFmName, who.getArtistId(), Integer.MAX_VALUE);
            if (ai.isEmpty()) {
                sendMessageQueue(e, ("Couldn't find any track of %s for %s%s".formatted(CommandUtil.escapeMarkdown(who.getArtist()), userString, timeframew.getDisplayString())));
                return;
            }
        } else {
            ai = lastFM.getTopArtistTracks(params.getLastFMData(), who.getArtist(), timeframew, artist);
            if (ai.isEmpty()) {
                sendMessageQueue(e, ("Couldn't find any %s track in %s's top 5k (or you don't have any track with more than 3 plays)%s!".formatted(CommandUtil.escapeMarkdown(who.getArtist()), userString, timeframew.getDisplayString())));
                return;
            }
        }
        String title = String.format("%s's top %s tracks%s", userString, who.getArtist(), timeframew.getDisplayString());
        String footer = "%s has listened to %d different %s songs!".formatted(userString, ai.size(), who.getArtist());
        RemainingImagesMode effectiveMode = CommandUtil.getEffectiveMode(params.getLastFMData().getRemainingImagesMode(), params);
        switch (effectiveMode) {
            case IMAGE, LIST -> {
                EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                        .setAuthor(title, PrivacyUtils.getLastFmArtistUserUrl(who.getArtist(), lastFmName), uInfo.getUrlImage())
                        .setThumbnail(CommandUtil.noImageUrl(who.getUrl()));

                if (ai.size() > 10) {

                    if (!StringUtils.isBlank(ai.get(0).getImageUrl())) {
                        embedBuilder.setFooter(footer, ai.get(0).getImageUrl());
                    } else {
                        embedBuilder.setFooter(footer);
                    }
                }
                new ListSender<>(e, ai, g -> ". **[" + CommandUtil.escapeMarkdown(g.getName()) + "](" + LinkUtils.getLastFMArtistTrack(who.getArtist(), g.getName()) + ")** - " + g.getPlays() + " plays" +
                                             "\n", embedBuilder)
                        .doSend();
            }
            case PIE -> {
                PieChart pieChart = this.pie.doPie(params, ai);
                pieChart.setTitle(title);
                BufferedImage image = new PieSetUp(footer, who.getUrl(), pieChart).setUp();
                sendImage(image, e);
            }
        }

    }
}
