package core.commands.moderation;

import com.google.common.collect.Lists;
import core.Chuu;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.apis.spotify.SpotifyUtils;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ButtonUtils;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.Confirmator;
import core.otherlisteners.Reactions;
import core.otherlisteners.util.ConfirmatorItem;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.utils.OptionalEntity;
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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class TrackListRefreshCommand extends ConcurrentCommand<ArtistAlbumParameters> {
    private final Spotify spotify;

    public TrackListRefreshCommand(ServiceView dao) {
        super(dao);
        this.spotify = SpotifySingleton.getInstance();
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
                .addOptional(new OptionalEntity("lastfm", "fetch from spotify", "lfm"))
                .addOptional(new OptionalEntity("force", "show all found options on spotify", "f"));
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

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setTitle("New tracklist for **%s** - **%s**".formatted(artist, album), LinkUtils.getLastFmArtistAlbumUrl(artist, album)).setThumbnail(cover);
        String description = trackList.stream().sorted(Comparator.comparingInt(Track::getPosition))
                .map(track -> "%s. %s".formatted(track.getPosition() + 1, track.getName()))
                .collect(Collectors.joining("\n"));
        return embedBuilder.setDescription(description);

    }

    private ConfirmatorResult obtainSend(Supplier<List<Track>> trackListSupplier, String service, long albumId, long artistId, Context e, String artist, String album, String cover) {
        List<Track> trackList = trackListSupplier.get();
        if (trackList.isEmpty()) {
            e.sendMessage("Couldn't find a tracklist for **%s** - **%s** on %s".formatted(artist, album, service)).queue();
            return null;
        }
        EmbedBuilder eb = generateEmbed(e, artist, album, cover, trackList);

        UnaryOperator<EmbedBuilder> timeOut = who -> who.setColor(CommandUtil.pastelColor()).clear().setTitle(String.format("Didn't change the tracklist for **%s** - **%s** using **%s**", artist, album, service))
                .setColor(Color.RED);

        List<ConfirmatorItem> confirms = List.of(
                new ConfirmatorItem(Reactions.ACCEPT, who -> who.clear().setColor(CommandUtil.pastelColor()).setTitle(String.format("Changed the tracklist for **%s** - **%s** using **%s**", artist, album, service)),
                        (z) -> {
                            db.deleteTracklist(albumId);
                            db.storeTrackList(albumId, artistId, new HashSet<>(trackList));
                        }),
                new ConfirmatorItem(Reactions.REJECT, timeOut, (z) -> {
                }));

        ActionRow of = ActionRow.of(ButtonUtils.danger("Cancel"), ButtonUtils.primary("Save tracklist"));
        return new ConfirmatorResult(List.of(of), message -> new Confirmator(eb, e, message, e.getAuthor().getIdLong(), confirms, timeOut, true, 60), eb);
    }

    private void doSend(Supplier<List<Track>> trackListSupplier, String service, long albumId, long artistId, Context e, String artist, String album, String cover) {
        ConfirmatorResult confirmatorResult = obtainSend(trackListSupplier, service, albumId, artistId, e, artist, album, cover);
        if (confirmatorResult == null) {
            return;
        }
        e.sendMessage(confirmatorResult.eb.build(), confirmatorResult.actionRows).queue(confirmatorResult.generator);
    }

    private void doConfirmation(Context e, String artist, String album, long albumId, long artistId, List<Spotify.AlbumResult> items) {

        EmbedBuilder eb = new ChuuEmbedBuilder(e)
                .setAuthor("Searching for %s | %s and found %d matching items".formatted(artist, album, items.size()));
        StringBuilder sb = new StringBuilder("Please select one from the following items:\n\n");
        List<List<Spotify.AlbumResult>> partition = Lists.partition(items.stream().limit(25).toList(), 5);

        List<ConfirmatorItem> ci = new ArrayList<>();
        Function<Spotify.AlbumResult, ConfirmatorResult> mapper = (Spotify.AlbumResult z) -> obtainSend(() -> spotify.getTracklistFromId(z.id(), z.artist()), "Spotify", albumId, artistId, e, z.artist(), z.album(), z.cover());

        AtomicInteger counter = new AtomicInteger();
        List<ActionRow> rows = partition.stream().map(w -> ActionRow.of(
                w.stream().map(z -> {
                            sb.append(counter.incrementAndGet()).append(": ").append("[%s](%s)%n".formatted(z.album(), SpotifyUtils.getAlbumLink(z.id())));
                            ci.add(new ConfirmatorItem(z.id(), r -> r, (m) -> {
                                ConfirmatorResult apply = mapper.apply(z);
                                if (apply != null) {
                                    m.editMessageComponents(apply.actionRows).setEmbeds(apply.eb.build()).queue(apply.generator);
                                }
                            }));
                            return Button.of(ButtonStyle.PRIMARY, z.id(), counter.get() + ": " + z.album());
                        }
                ).toList())
        ).toList();
        e.sendMessage(eb.setDescription(sb).build(), rows).queue(finalMesage ->
                new Confirmator(eb, e, finalMesage, e.getAuthor().getIdLong(), ci,
                        embedBuilder -> embedBuilder.setColor(Color.RED).setDescription("Didn't select any album!"), true, 120));

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
        String cover = Chuu.getCoverService().getCover(albumId, sAlb.getUrl(), e);
        String album = sAlb.getAlbum();

        TracklistService tracklistService = new GlobalTracklistService(db);
        if (params.hasOptional("spotify")) {
            List<Spotify.AlbumResult> albumResults = spotify.searchAlbums(artist, album);
            if (albumResults.size() > 1) {
                Spotify.AlbumResult albumResult = albumResults.get(0);
                if (!albumResult.album().equalsIgnoreCase(album) || !albumResult.artist().equalsIgnoreCase(artist) || params.hasOptional("--force")) {
                    doConfirmation(e, artist, album, albumId, artistId, albumResults);
                    return;
                }
            }
            doSend(() -> tracklistService.getSpotifyTracklist(artist, album), "Spotify", albumId, artistId, e, artist, album, cover);
            return;
        } else if (params.hasOptional("musicbrainz")) {
            obtainSend(() -> tracklistService.getMusicBrainzTracklist(sAlb.getAlbumMbid(), artist, album), "Musicbrainz", albumId, artistId, e, artist, album, cover);
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

    private record ConfirmatorResult(List<ActionRow> actionRows, Consumer<Message> generator,
                                     EmbedBuilder eb) {

    }

}
