package core.commands.streaks;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.util.bubble.StringFrequency;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.utils.Optionals;
import core.services.tags.TagStorer;
import core.services.validators.AlbumValidator;
import core.services.validators.ArtistValidator;
import core.services.validators.TrackValidator;
import core.util.ServiceView;
import dao.entities.*;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.collections4.Bag;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Credits: to flushed_emoji bot owner for the idea. Aka stolen completely
 */
public class TagStreakCommand extends ConcurrentCommand<ChuuDataParams> {

    public TagStreakCommand(ServiceView dao) {
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
    public void onCommand(Context e, @Nonnull ChuuDataParams params) throws LastFmException {


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
                    ScrobbledArtist sa = new ArtistValidator(db, lastFM, e).validate(artist, true);
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


        if (tagCombo.isEmpty()) {
            sendMessageQueue(e, "Couldn't find any tag combo on your history :(");
            return;
        }

        List<String> tags = tagCombo.stream().map(t -> new StringFrequency(t.getName(), tagCombo.getCount(t))).filter(t -> t.freq() > 1)
                .sorted(Comparator.comparingInt(StringFrequency::freq).reversed())
                .map(z -> "**[%s](%s)**: ".formatted(z.key(), LinkUtils.getLastFmTagUrl(z.key())) +
                        z.freq() + (z.freq() >= 2000 ? "+" : "") + " consecutive plays\n").toList();


        DiscordUserDisplay userInformation = CommandUtil.getUserInfoEscaped(e, discordID);
        String userName = userInformation.username();
        String userUrl = userInformation.urlImage();

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s's current tag streak", CommandUtil.unescapedUser(userName, discordID, e)), CommandUtil.getLastFmUser(lastfmId), userUrl);
        new PaginatorBuilder<>(e, embedBuilder, tags).pageSize(5).unnumered().build().queue();

    }
}
