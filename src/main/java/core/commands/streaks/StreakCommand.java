package core.commands.streaks;

import core.Chuu;
import core.apis.last.queues.TrackGroupAlbumQueue;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.utils.Optionals;
import core.services.validators.ArtistValidator;
import core.util.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.StreakEntity;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Credits: to lfmwhoknows bot owner for the idea
 */
public class StreakCommand extends ConcurrentCommand<ChuuDataParams> {

    public StreakCommand(ServiceView dao) {
        super(dao);

    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.STREAKS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db, Optionals.START.opt);
    }

    @Override
    public String getDescription() {
        return "Last playing streak";
    }

    @Override
    public List<String> getAliases() {
        return List.of("streak", "combo");
    }

    @Override
    public String getName() {
        return "Streak";
    }

    @Override
    public void onCommand(Context e, @NotNull ChuuDataParams params) throws LastFmException {


        LastFMData user = params.getLastFMData();
        String lastfmId = user.getName();
        long discordID = user.getDiscordId();


        DiscordUserDisplay userInformation = CommandUtil.getUserInfoEscaped(e, discordID);
        String userName = userInformation.username();
        String userUrl = userInformation.urlImage();
        StreakEntity combo = lastFM.getCombo(user);

        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(combo.getCurrentArtist(), false);

        Long albumId = null;
        if (combo.albumCount() > 1) {
            albumId = CommandUtil.albumvalidate(db, sA, lastFM, combo.getCurrentAlbum()).id();
        }

        if (combo.artistCount() >= 3) {
            if (combo.trackCount() >= StreakEntity.MAX_STREAK || combo.getStreakStart().isBefore(Instant.EPOCH.plus(350, ChronoUnit.DAYS))) {
                //Only one allowed Max Streak per user
                combo.setStreakStart(Instant.EPOCH.plus(1, ChronoUnit.DAYS));
            }
            Long fId = albumId;
            CommandUtil.runLog(() -> db.insertCombo(combo, discordID, sA.getArtistId(), fId));
        }


        int artistPlays = db.getArtistPlays(sA.getArtistId(), lastfmId);
        String aString = CommandUtil.escapeMarkdown(sA.getArtist());
        StringBuilder description = new StringBuilder();
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s 's current listening streak", CommandUtil.unescapedUser(userName, discordID, e)), CommandUtil.getLastFmUser(lastfmId), userUrl)
                .setThumbnail(CommandUtil.noImageUrl(sA.getUrl()))
                .setDescription("");

        if (combo.artistCount() > 1) {
            description.append("**Artist**: ")
                    .append(combo.artistCount()).append(combo.artistCount() >= 9000 ? "+" : "").append(combo.artistCount() != 1 ? " consecutive plays - " : " play - ")
                    .append("**[").append(aString).append("](").append(LinkUtils.getLastFmArtistUrl(combo.getCurrentArtist())).append(")**").append("\n");
        }
        if (combo.albumCount() > 1) {
            if (combo.getUrl() == null || combo.getUrl().isBlank() || combo.getUrl().equals(TrackGroupAlbumQueue.defaultTrackImage)) {
                String s = null;
                if (albumId != null) {
                    s = Chuu.getCoverService().getCover(albumId, null, e);
                }
                if (s != null && !s.isBlank()) {
                    embedBuilder.setThumbnail(s);
                }
            } else {
                embedBuilder.setThumbnail(combo.getUrl());
            }
            description.append("**Album**: ")
                    .append(combo.albumCount())
                    .append(combo.albumCount() >= StreakEntity.MAX_STREAK ? "+" : "")
                    .append(combo.albumCount() != 1 ? " consecutive plays - " : " play - ")
                    .append("**[").append(CommandUtil.escapeMarkdown(combo.getCurrentAlbum())).append("](")
                    .append(LinkUtils.getLastFmArtistAlbumUrl(combo.getCurrentArtist(), combo.getCurrentAlbum())).append(")**")
                    .append("\n");
        }
        if (combo.trackCount() > 1) {
            if (combo.getUrl() != null && !combo.getUrl().isBlank() && !combo.getUrl().equals(TrackGroupAlbumQueue.defaultTrackImage)) {
                embedBuilder.setThumbnail(combo.getUrl());
            }
            description.append("**Song**: ").append(combo.trackCount()).append(combo.trackCount() >= StreakEntity.MAX_STREAK ? "+" : "")
                    .append(combo.trackCount() != 1 ? " consecutive plays - " : " play - ").append("**[")
                    .append(CommandUtil.escapeMarkdown(combo.getCurrentSong())).append("](").append(LinkUtils.getLastFMArtistTrack(combo.getCurrentArtist(), combo.getCurrentSong())).append(")**").append("\n");
        }
        if (params.hasOptional("start")) {
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(combo.getStreakStart(), user.getTimeZone().toZoneId());
            if (combo.getStreakStart().isBefore(Instant.EPOCH.plus(60 * 60 * 24 * 365, ChronoUnit.SECONDS))) {
                description.append("**Started**: ").append("**-**").append("\n");
            } else {
                String day = offsetDateTime.toLocalDate().format(DateTimeFormatter.ISO_DATE);
                String date = CommandUtil.getDateTimestampt(combo.getStreakStart());
                String link = String.format("%s/library?from=%s&rangetype=1day", PrivacyUtils.getLastFmUser(user.getName()), day);
                description.append("**Started**: ").append("**[").append(date).append("](")
                        .append(link).append(")**")
                        .append("\n");
            }
        }

        embedBuilder.setDescription(description)
                .setFooter(String.format("%s has played %s %d %s!", CommandUtil.unescapedUser(userName, discordID, e), sA.getArtist(), artistPlays, CommandUtil.singlePlural(artistPlays, "time", "times")));
        e.sendMessage(embedBuilder.build()).
                queue();

    }
}
