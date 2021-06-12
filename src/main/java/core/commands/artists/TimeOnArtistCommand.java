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
import core.imagerenderer.util.pie.PieSetUp;
import core.parsers.ArtistTimeFrameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistTimeFrameParameters;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.*;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchart.PieChart;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

public class TimeOnArtistCommand extends ConcurrentCommand<ArtistTimeFrameParameters> {

    private final DiscogsApi discogs;
    private final Spotify spotify;
    private final IPieableList<Track, CommandParameters> pie;

    public TimeOnArtistCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = true;
        this.discogs = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
        this.pie = DefaultList.fillPie(Track::getPie, Track::getDuration);

    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistTimeFrameParameters> initParser() {
        return new ArtistTimeFrameParser(db, lastFM, true, new OptionalEntity("list", "break it down by track"));
    }

    @Override
    public String getDescription() {
        return "Your time spent on an artist";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("time", "artisttime");
    }

    @Override
    public String slashName() {
        return "specific-time";
    }

    @Override
    public String getName() {

        return "Time on an artist";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistTimeFrameParameters params) throws LastFmException {

        long userId = params.getLastFMData().getDiscordId();
        TimeFrameEnum timeframew = params.getTimeFrame();
        String artist = params.getArtist();
        ScrobbledArtist who = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(db, who, lastFM, discogs, spotify, true, !params.isNoredirect());
        List<Track> ai;
        String lastFmName = params.getLastFMData().getName();

        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, userId);
        String userString = uInfo.getUsername();

        if (timeframew.equals(TimeFrameEnum.ALL)) {
            ai = db.getTopArtistTracksDuration(lastFmName, who.getArtistId(), Integer.MAX_VALUE);
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
        String fullStr = "%s has listen to **%s** distinct **%s** tracks (%s scrobbles) for a total of **%s**!".formatted(uInfo.getUsername(),
                ai.size(),
                who.getArtist(),
                ai.stream().mapToInt(Track::getPlays).sum(),
                CommandUtil.secondFormatter(ai.stream().mapToInt(Track::getDuration).sum()));
        RemainingImagesMode effectiveMode = CommandUtil.getEffectiveMode(params.getLastFMData().getRemainingImagesMode(), params);
        String title = String.format("%s's top %s tracks%s by time", userString, who.getArtist(), timeframew.getDisplayString());
        switch (effectiveMode) {
            case IMAGE -> sendMessageQueue(e, fullStr);
            case LIST -> {
                String footer = fullStr.replaceAll("\\*\\*", "");
                EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                        .setAuthor(title, PrivacyUtils.getLastFmArtistUserUrl(who.getArtist(), lastFmName), uInfo.getUrlImage())
                        .setThumbnail(CommandUtil.noImageUrl(who.getUrl()));

                if (!StringUtils.isBlank(ai.get(0).getImageUrl())) {
                    embedBuilder.setFooter(footer, ai.get(0).getImageUrl());
                } else {
                    embedBuilder.setFooter(footer);
                }
                new ListSender<>(e, ai, g -> ". **[%s](%s)** - %s (%d plays)\n".formatted(CommandUtil.escapeMarkdown(g.getName()), LinkUtils.getLastFMArtistTrack(who.getArtist(), g.getName()),
                        CommandUtil.secondFormatter(g.getDuration() * g.getPlays()),
                        g.getPlays()), embedBuilder)
                        .doSend();
            }
            case PIE -> {
                String footer = fullStr.replaceAll("\\*\\*", "");
                PieChart pieChart = pie.doPie(params, ai);
                pieChart.setTitle(title);
                sendImage(new PieSetUp(footer, who.getUrl(), pieChart).setUp(), e);
            }
        }
    }
}
