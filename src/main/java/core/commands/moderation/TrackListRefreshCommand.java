package core.commands.moderation;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ButtonUtils;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.otherlisteners.Confirmator;
import core.otherlisteners.Reactions;
import core.otherlisteners.util.ConfirmatorItem;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.utils.OptionalEntity;
import core.services.CoverService;
import core.services.tracklist.GlobalTracklistService;
import core.services.tracklist.TracklistService;
import core.services.validators.AlbumValidator;
import dao.ServiceView;
import dao.entities.Role;
import dao.entities.ScrobbledAlbum;
import dao.entities.Track;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class TrackListRefreshCommand extends ConcurrentCommand<ArtistAlbumParameters> {

    public TrackListRefreshCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistAlbumParser(db, lastFM)
                .addOptional(new OptionalEntity("musicbrainz", "fetch from spotify", "mb"))
                .addOptional(new OptionalEntity("spotify", "fetch from spotify", "sp"))
                .addOptional(new OptionalEntity("lastfm", "fetch from spotify", "lfm"));
    }

    @Override
    public String getDescription() {
        return "Updates tracklists";
    }

    @Override
    public List<String> getAliases() {
        return List.of("refreshtracklist", "refreshtl", "rtl");
    }

    @Override
    public String getName() {
        return "Tracklist refresh";
    }

    private EmbedBuilder generateEmbed(Context e, String artist, String album, String cover, List<Track> trackList) {

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setTitle("New tracklist for **%s** - **%s**".formatted(artist, album), LinkUtils.getLastFmArtistAlbumUrl(artist, album)).setImage(cover);
        String description = trackList.stream().sorted(Comparator.comparingInt(Track::getPosition)).map(track -> "%s. %s".formatted(track.getPosition() + 1, track.getName())).collect(Collectors.joining("\n"));
        return embedBuilder.setDescription(description);

    }


    private void doSend(Supplier<List<Track>> trackListSupplier, String service, long albumId, long artistId, Context e, String artist, String album, String cover) {
        List<Track> trackList = trackListSupplier.get();
        if (trackList.isEmpty()) {
            e.sendMessage("Couldn't find a tracklist for **%s** - **%s** on %s".formatted(artist, album, service)).queue();
            return;
        }
        EmbedBuilder eb = generateEmbed(e, artist, album, cover, trackList);

        UnaryOperator<EmbedBuilder> timeOut = who -> who.clear().setTitle(String.format("Didn't change the tracklist for **%s** - **%s** using **%s**", artist, album, service))
                .setColor(Color.RED);

        List<ConfirmatorItem> confirms = List.of(
                new ConfirmatorItem(Reactions.ACCEPT, who -> who.clear().setTitle(String.format("Changed the tracklist for **%s** - **%s** using **%s**", artist, album, service)),
                        (z) -> {
                            db.deleteTracklist(albumId);
                            db.storeTrackList(albumId, artistId, new HashSet<>(trackList));
                        }),
                new ConfirmatorItem(Reactions.REJECT, timeOut, (z) -> {
                }));

        ActionRow of = ActionRow.of(ButtonUtils.danger("Cancel"), ButtonUtils.primary("Save tracklist"));

        e.sendMessage(eb.build(), of).queue(message -> new Confirmator(eb, e, message, e.getAuthor().getIdLong(), confirms, timeOut, true, 60));
    }

    @Override
    public void onCommand(Context e, @NotNull ArtistAlbumParameters params) throws LastFmException, InstanceNotFoundException {

        if (params.getLastFMData().getRole() != Role.ADMIN) {
            sendMessage(e, "Only bot admins can use this command!").queue();
            return;
        }

        ScrobbledAlbum sAlb = new AlbumValidator(db, lastFM).validate(params.getArtist(), params.getAlbum());
        long albumId = sAlb.getAlbumId();
        long artistId = sAlb.getArtistId();
        String artist = sAlb.getArtist();
        String cover = new CoverService(db).getCover(albumId, null, e);
        String album = sAlb.getAlbum();

        TracklistService tracklistService = new GlobalTracklistService(db);
        if (params.hasOptional("spotify")) {
            doSend(() -> tracklistService.getSpotifyTracklist(artist, album), "Spotify", albumId, artistId, e, artist, album, cover);
            return;
        } else if (params.hasOptional("musicbrainz")) {
            doSend(() -> tracklistService.getMusicBrainzTracklist(sAlb.getAlbumMbid(), artist, album), "Musicbrainz", albumId, artistId, e, artist, album, cover);
            return;
        } else if (params.hasOptional("lastfm")) {
            doSend(() -> {
                try {
                    return tracklistService.getLastFmTracklist(params.getLastFMData(), artist, album).getTrackList();
                } catch (LastFmException ex) {
                    return Collections.emptyList();
                }
            }, "Last.fm", albumId, artistId, e, artist, album, cover);
            return;
        }
        sendMessage(e, "Please specify the service to update the tracklist with").queue();
    }

}
