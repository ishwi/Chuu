package core.commands.stats;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.queues.TrackGroupAlbumQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.StreakEntity;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Credits: to lfmwhoknows bot owner for the idea
 */
public class StreakCommand extends ConcurrentCommand<ChuuDataParams> {
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;

    public StreakCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();

    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db, new OptionalEntity("start", "show the moment the streak started"));
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
    protected void onCommand(Context e, @NotNull ChuuDataParams params) throws LastFmException {


        LastFMData user = params.getLastFMData();
        String lastfmId = user.getName();
        long discordID = user.getDiscordId();


        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordID);
        String userName = userInformation.getUsername();
        String userUrl = userInformation.getUrlImage();
        StreakEntity combo = lastFM.getCombo(user);

        ScrobbledArtist artist = new ScrobbledArtist(combo.getCurrentArtist(), 0, "");
        CommandUtil.validate(db, artist, lastFM, discogsApi, spotifyApi);
        Long albumId = null;
        if (combo.getAlbCounter() > 1) {
            albumId = CommandUtil.albumvalidate(db, artist, lastFM, combo.getCurrentAlbum());
        }

        if (combo.getaCounter() >= 3) {
            if (combo.gettCounter() >= StreakEntity.MAX_STREAK) {
                //Only one allowed Max Streak per user
                combo.setStreakStart(Instant.EPOCH.plus(1, ChronoUnit.DAYS));
            }
            Long fId = albumId;
            CompletableFuture.runAsync(() -> db.insertCombo(combo, discordID, artist.getArtistId(), fId));
        }


        int artistPlays = db.getArtistPlays(artist.getArtistId(), lastfmId);
        String aString = CommandUtil.cleanMarkdownCharacter(artist.getArtist());
        StringBuilder description = new StringBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(String.format("%s 's current listening streak", CommandUtil.markdownLessUserString(userName, discordID, e)), CommandUtil.getLastFmUser(lastfmId), userUrl)
                .setThumbnail(CommandUtil.noImageUrl(artist.getUrl()))
                .setDescription("");

        if (combo.getaCounter() > 1) {
            description.append("**Artist**: ")
                    .append(combo.getaCounter()).append(combo.getaCounter() >= 9000 ? "+" : "").append(combo.getaCounter() != 1 ? " consecutive plays - " : " play - ")
                    .append("**[").append(aString).append("](").append(LinkUtils.getLastFmArtistUrl(combo.getCurrentArtist())).append(")**").append("\n");
        }
        if (combo.getAlbCounter() > 1) {
            if (combo.getUrl() == null || combo.getUrl().isBlank() || combo.getUrl().equals(TrackGroupAlbumQueue.defaultTrackImage)) {
                String s = CommandUtil.albumUrl(db, lastFM, combo.getCurrentArtist(), combo.getCurrentAlbum(), discogsApi, spotifyApi);
                if (s != null && !s.isBlank()) {
                    embedBuilder.setThumbnail(s);
                }
            } else {
                embedBuilder.setThumbnail(combo.getUrl());
            }
            description.append("**Album**: ")
                    .append(combo.getAlbCounter())
                    .append(combo.getAlbCounter() >= 9000 ? "+" : "")
                    .append(combo.getAlbCounter() != 1 ? " consecutive plays - " : " play - ")
                    .append("**[").append(CommandUtil.cleanMarkdownCharacter(combo.getCurrentAlbum())).append("](")
                    .append(LinkUtils.getLastFmArtistAlbumUrl(combo.getCurrentArtist(), combo.getCurrentAlbum())).append(")**")
                    .append("\n");
        }
        if (combo.gettCounter() > 1) {
            if (combo.getUrl() != null && !combo.getUrl().isBlank() && !combo.getUrl().equals(TrackGroupAlbumQueue.defaultTrackImage)) {
                embedBuilder.setThumbnail(combo.getUrl());
            }
            description.append("**Song**: ").append(combo.gettCounter()).append(combo.gettCounter() >= 9000 ? "+" : "")
                    .append(combo.gettCounter() != 1 ? " consecutive plays - " : " play - ").append("**[")
                    .append(CommandUtil.cleanMarkdownCharacter(combo.getCurrentSong())).append("](").append(LinkUtils.getLastFMArtistTrack(combo.getCurrentArtist(), combo.getCurrentSong())).append(")**").append("\n");
        }
        if (params.hasOptional("start")) {
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(combo.getStreakStart(), user.getTimeZone().toZoneId());
            if (combo.getStreakStart().isBefore(Instant.EPOCH.plus(60 * 60 * 24 * 365, ChronoUnit.SECONDS))) {
                description.append("**Started**: ").append("**-**").append("\n");
            } else {
                String day = offsetDateTime.toLocalDate().format(DateTimeFormatter.ISO_DATE);
                String date = CommandUtil.getAmericanizedDate(offsetDateTime);
                String link = String.format("%s/library?from=%s&rangetype=1day", PrivacyUtils.getLastFmUser(user.getName()), day);
                description.append("**Started**: ").append("**[").append(date).append("](")
                        .append(link).append(")**")
                        .append("\n");
            }
        }

        embedBuilder.setDescription(description)
                .setColor(ColorService.computeColor(e))
                .setFooter(String.format("%s has played %s %d %s!", CommandUtil.markdownLessUserString(userName, discordID, e), artist.getArtist(), artistPlays, CommandUtil.singlePlural(artistPlays, "time", "times")));
        e.sendMessage(embedBuilder.build()).
                queue();

    }
}
