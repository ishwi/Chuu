package core.commands.artists;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.*;
import core.exceptions.LastFmException;
import core.imagerenderer.util.pie.DefaultList;
import core.imagerenderer.util.pie.IPieableList;
import core.imagerenderer.util.pie.OptionalPie;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ArtistTimeFrameParser;
import core.parsers.Parser;
import core.parsers.params.ArtistTimeFrameParameters;
import core.parsers.params.CommandParameters;
import core.parsers.utils.OptionalEntity;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.*;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchart.PieChart;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class TimeOnArtistCommand extends ConcurrentCommand<ArtistTimeFrameParameters> {

    private final IPieableList<Track, CommandParameters> pie;

    public TimeOnArtistCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = true;
        this.pie = DefaultList.fillPie(Track::getPie, Track::getDuration);
        new OptionalPie(getParser());

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
        return "specific-artist";
    }

    @Override
    public String getName() {

        return "Time on an artist";
    }

    @Override
    public void onCommand(Context e, @Nonnull ArtistTimeFrameParameters params) throws LastFmException {

        long userId = params.getLastFMData().getDiscordId();
        TimeFrameEnum timeframew = params.getTimeFrame();
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), !params.isNoredirect());
        String artist = sA.getArtist();
        List<Track> ai;
        String lastFmName = params.getLastFMData().getName();

        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, userId);
        String userString = uInfo.username();

        if (timeframew.equals(TimeFrameEnum.ALL)) {
            ai = db.getTopArtistTracksDuration(lastFmName, sA.getArtistId(), Integer.MAX_VALUE);
            if (ai.isEmpty()) {
                sendMessageQueue(e, ("Couldn't find any track of %s for %s%s".formatted(CommandUtil.escapeMarkdown(sA.getArtist()), userString, timeframew.getDisplayString())));
                return;
            }
        } else {
            ai = lastFM.getTopArtistTracks(params.getLastFMData(), sA.getArtist(), timeframew, artist);
            if (ai.isEmpty()) {
                sendMessageQueue(e, ("Couldn't find any %s track in %s's top 5k (or you don't have any track with more than 3 plays)%s!".formatted(CommandUtil.escapeMarkdown(sA.getArtist()), userString, timeframew.getDisplayString())));
                return;
            }
        }
        String fullStr = "%s has listened to **%s** distinct **%s** tracks (%s scrobbles) for a total of **%s**!".formatted(uInfo.username(),
                ai.size(),
                sA.getArtist(),
                ai.stream().mapToInt(Track::getPlays).sum(),
                CommandUtil.secondFormatter(ai.stream().mapToInt(w -> w.getDuration() * w.getPlays()).sum()));
        RemainingImagesMode effectiveMode = CommandUtil.getEffectiveMode(params.getLastFMData().getRemainingImagesMode(), params);
        String title = String.format("%s's top %s tracks%s by time", userString, sA.getArtist(), timeframew.getDisplayString());
        switch (effectiveMode) {
            case IMAGE -> sendMessageQueue(e, fullStr);
            case LIST -> {
                String footer = fullStr.replaceAll("\\*\\*", "");
                EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                        .setAuthor(title, PrivacyUtils.getLastFmArtistUserUrl(sA.getArtist(), lastFmName), uInfo.urlImage())
                        .setThumbnail(CommandUtil.noImageUrl(sA.getUrl()));

                if (!StringUtils.isBlank(ai.get(0).getImageUrl())) {
                    embedBuilder.setFooter(footer, ai.get(0).getImageUrl());
                } else {
                    embedBuilder.setFooter(footer);
                }
                new PaginatorBuilder<>(e, embedBuilder, ai)
                        .mapper(g -> ". **[%s](%s)** - %s (%d plays)\n".formatted(CommandUtil.escapeMarkdown(g.getName()), LinkUtils.getLastFMArtistTrack(sA.getArtist(), g.getName()), CommandUtil.secondFormatter(g.getDuration() * g.getPlays()), g.getPlays()))
                        .build().queue();
            }
            case PIE -> {
                String footer = fullStr.replaceAll("\\*\\*", "");
                PieChart pieChart = pie.doPie(params, ai);
                pieChart.setTitle(title);
                sendImage(new PieDoer(footer, sA.getUrl(), pieChart).fill(), e);
            }
        }
    }
}
