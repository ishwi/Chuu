package core.commands.albums;

import core.commands.Context;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PieDoer;
import core.exceptions.LastFmException;
import core.imagerenderer.TrackDistributor;
import core.imagerenderer.util.pie.IPieableList;
import core.imagerenderer.util.pie.PieableListTrack;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.utils.Optionals;
import core.services.tracklist.TracklistService;
import core.services.tracklist.UserTrackListService;
import core.services.validators.AlbumValidator;
import core.util.ServiceView;
import dao.entities.FullAlbumEntity;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;
import dao.entities.Track;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AlbumTracksDistributionCommand extends AlbumPlaysCommand {
    private final IPieableList<Track, ArtistAlbumParameters> pie;

    public AlbumTracksDistributionCommand(ServiceView dao) {
        super(dao);
        this.pie = new PieableListTrack(this.getParser());
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public String getDescription() {
        return "Plays on each track of the provided album";
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        ArtistAlbumParser parser = new ArtistAlbumParser(db, lastFM, Optionals.LIST.opt, Optionals.PLAYS.opt.withDescription("sort by plays instead of the normal order"));
        parser.setExpensiveSearch(true);
        return parser;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("tracks", "tl");
    }

    @Override
    public String getName() {
        return "Track Distribution";
    }

    @Override
    protected void doSomethingWithAlbumArtist(ScrobbledArtist scrobbledArtist, String album, Context e, long who, ArtistAlbumParameters params) throws LastFmException {

        String artist = scrobbledArtist.getArtist();

        ScrobbledAlbum sAlb = new AlbumValidator(db, lastFM).validate(scrobbledArtist.getArtistId(), artist, album);

        TracklistService tracklistService = new UserTrackListService(db, params.getLastFMData().getName());

        Optional<FullAlbumEntity> trackList = tracklistService.getTrackList(sAlb, params.getLastFMData(), scrobbledArtist.getUrl(), e);
        if (trackList.isEmpty()) {
            sendMessageQueue(e, "Couldn't find a tracklist for " + CommandUtil.escapeMarkdown(artist
            ) + " - " + CommandUtil.escapeMarkdown(sAlb.getAlbum()));
        } else {

            FullAlbumEntity fullAlbumEntity = trackList.get();
            fullAlbumEntity.setArtistUrl(scrobbledArtist.getUrl());
            if (params.hasOptional("plays")) {
                fullAlbumEntity.setTrackList(fullAlbumEntity.getTrackList().stream().sorted(Comparator.comparingInt(Track::getPlays)).collect(Collectors.toList()));
            }
            switch (CommandUtil.getEffectiveMode(params.getLastFMData().getRemainingImagesMode(), params)) {
                case IMAGE -> {
                    BufferedImage bufferedImage = TrackDistributor.drawImage(fullAlbumEntity);
                    sendImage(bufferedImage, e);
                }
                case PIE -> {

                    PieChart pieChart = this.pie.doPie(params, fullAlbumEntity.getTrackList());
                    pieChart.setTitle(artist + " - " + sAlb.getAlbum() + " tracklist");
                    BufferedImage bufferedImage = new PieDoer("", fullAlbumEntity.getAlbumUrl(), pieChart).fill();


                    sendImage(bufferedImage, params.getE());
                }
                case LIST -> {
                    EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                            .setTitle(String.format("%s tracklist", album), LinkUtils.getLastFmArtistAlbumUrl(artist, album))
                            .setFooter(String.format("%s has %d total plays on the album!%n", CommandUtil.unescapedUser(getUserString(e, params.getLastFMData().getDiscordId()), params.getLastFMData().getDiscordId(), e), fullAlbumEntity.getTotalPlayNumber()), null)
                            .setThumbnail(fullAlbumEntity.getAlbumUrl());

                    new PaginatorBuilder<>(e, embedBuilder, fullAlbumEntity.getTrackList()).mapper(t -> ". " + "[" +
                                    CommandUtil.escapeMarkdown(t.getName()) +
                                    "](" + LinkUtils.getLastFMArtistTrack(artist, t.getName()) +
                                    ")" + " - " + t.getPlays() + CommandUtil.singlePlural(t.getPlays(), " play", " plays") + "\n")
                            .build().queue();

                }
            }
        }
    }


}
