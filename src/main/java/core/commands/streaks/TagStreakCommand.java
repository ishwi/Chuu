package core.commands.streaks;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.util.bubble.StringFrequency;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.services.AlbumValidator;
import core.services.TrackValidator;
import core.services.tags.TagStorer;
import dao.ChuuService;
import dao.entities.*;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.collections4.Bag;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Credits: to flushed_emoji bot owner for the idea. Aka stolen completely
 */
public class TagStreakCommand extends ConcurrentCommand<ChuuDataParams> {

    public TagStreakCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.STREAKS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db, new OptionalEntity("start", "show the moment the streak started"));
    }

    @Override
    public String getDescription() {
        return "Genre combo";
    }

    @Override
    public List<String> getAliases() {
        return List.of("tagstreak", "tagcombo", "genrecombo", "genrestreak", "tcombo", "tstreak");
    }

    @Override
    public String getName() {
        return "Genre streak";
    }

    private Set<Genre> map(Collection<String> genres) {
        return genres.stream().map(Genre::new).collect(Collectors.toSet());
    }

    @Override
    protected void onCommand(Context e, @NotNull ChuuDataParams params) throws LastFmException {


        LastFMData user = params.getLastFMData();
        String lastfmId = user.getName();
        long discordID = user.getDiscordId();
        Map<String, Long> artistToId = new HashMap<>();
        Map<AlbumInfo, Long> albumToId = new HashMap<>();
        Map<TrackInfo, Long> trackToId = new HashMap<>();
        Bag<Genre> tagCombo = lastFM.getTagCombo(user, (artist, track, album) -> {
            Set<Genre> accumTags = null;
            try {
                Long artistId = artistToId.get(artist);
                if (artistId == null) {
                    ScrobbledArtist sa = new ScrobbledArtist(artist, 0, null);
                    CommandUtil.validate(db, sa, lastFM, null, null, false, true);
                    artistToId.put(artist, sa.getArtistId());
                    artistId = sa.getArtistId();
                }
                accumTags = db.getArtistTag(artistId).stream().map(Genre::new).collect(Collectors.toSet());

                TrackInfo ti = new TrackInfo(artist, album, track, null);
                Long trackId = trackToId.get(ti);
                if (trackId == null) {
                    ScrobbledTrack sTr = new TrackValidator(db, lastFM).validate(artistId, artist, track);
                    trackToId.put(ti, sTr.getTrackId());
                    trackId = sTr.getTrackId();
                }
                accumTags.addAll(map(db.getTrackTags(trackId)));

                if (album != null) {
                    AlbumInfo ai = new AlbumInfo(album, artist);
                    Long albumId = albumToId.get(ai);
                    if (albumId == null) {
                        ScrobbledAlbum sAlb = new AlbumValidator(db, lastFM).validate(artistId, artist, album);
                        albumToId.put(ai, sAlb.getAlbumId());
                        albumId = sAlb.getAlbumId();
                    }
                    accumTags.addAll(map(db.getAlbumTags(albumId)));
                }
                if (accumTags.isEmpty()) {
                    accumTags = map(new TagStorer(db, lastFM, executor, new NowPlayingArtist(artist, null, true, album, track, null, lastfmId, false)).findTags(15));
                }
                return accumTags;
            } catch (Exception ex) {
                Chuu.getLogger().warn(ex.getMessage(), ex);
                if (accumTags == null || accumTags.isEmpty())
                    return null;
                return accumTags;
            }
        });


        List<String> tags = tagCombo.stream().map(t -> new StringFrequency(t.getName(), tagCombo.getCount(t))).filter(t -> t.freq() > 1)
                .sorted(Comparator.comparingInt(StringFrequency::freq).reversed())
                .map(z -> "**[%s](%s)**: ".formatted(z.key(), LinkUtils.getLastFmTagUrl(z.key())) +
                          z.freq() + (z.freq() >= 2000 ? "+" : "") + " consecutive plays\n").toList();
        if (tags.isEmpty()) {
            sendMessageQueue(e, "Couldn't find any tag combo on your history :(");
            return;
        }

        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordID);
        String userName = userInformation.getUsername();
        String userUrl = userInformation.getUrlImage();

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s's current tag streak", CommandUtil.markdownLessUserString(userName, discordID, e)), CommandUtil.getLastFmUser(lastfmId), userUrl)
                .setDescription(tags.stream().limit(5).collect(Collectors.joining()));
        e.sendMessage(embedBuilder.build()).
                queue(m -> new Reactionary<>(tags, m, 5, embedBuilder, false));
    }
}
