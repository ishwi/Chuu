package core.commands.albums;

import core.commands.Context;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.TrackDistributor;
import core.imagerenderer.util.pie.IPieableList;
import core.imagerenderer.util.pie.PieableListTrack;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistAlbumParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.services.AlbumValidator;
import core.services.tracklist.TracklistService;
import core.services.tracklist.UserTrackListService;
import dao.ServiceView;
import dao.entities.FullAlbumEntity;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;
import dao.entities.Track;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.imgscalr.Scalr;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        ArtistAlbumParser parser = new ArtistAlbumParser(db, lastFM);
        parser.addOptional(new OptionalEntity("list", "display in list format"));

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

        ScrobbledAlbum scrobbledAlbum = new AlbumValidator(db, lastFM).validate(scrobbledArtist.getArtistId(), artist, album);

        TracklistService tracklistService = new UserTrackListService(db, params.getLastFMData().getName());

        Optional<FullAlbumEntity> trackList = tracklistService.getTrackList(scrobbledAlbum, params.getLastFMData(), scrobbledArtist.getUrl(), e);
        if (trackList.isEmpty()) {
            sendMessageQueue(e, "Couldn't find a tracklist for " + CommandUtil.escapeMarkdown(artist
            ) + " - " + CommandUtil.escapeMarkdown(scrobbledAlbum.getAlbum()));
        } else {

            FullAlbumEntity fullAlbumEntity = trackList.get();

            switch (CommandUtil.getEffectiveMode(params.getLastFMData().getRemainingImagesMode(), params)) {
                case IMAGE -> {
                    BufferedImage bufferedImage = TrackDistributor.drawImage(fullAlbumEntity);
                    sendImage(bufferedImage, e);
                }
                case PIE -> {
                    PieChart pieChart = this.pie.doPie(params, fullAlbumEntity.getTrackList());
                    pieChart.setTitle(params.getArtist() + " - " + params.getAlbum() + " tracklist");
                    BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = bufferedImage.createGraphics();
                    GraphicUtils.setQuality(g);
                    pieChart.paint(g, 1000, 750);
                    BufferedImage image = GraphicUtils.getImage(fullAlbumEntity.getAlbumUrl());
                    if (image != null) {
                        BufferedImage backgroundImage = Scalr.resize(image, 150);
                        g.drawImage(backgroundImage, 10, 750 - 10 - backgroundImage.getHeight(), null);
                    }
                    sendImage(bufferedImage, params.getE());
                }
                case LIST -> {
                    StringBuilder a = new StringBuilder();
                    List<String> lines = fullAlbumEntity.getTrackList().stream().map(t -> ". " + "[" +
                                                                                          CommandUtil.escapeMarkdown(t.getName()) +
                                                                                          "](" + LinkUtils.getLastFMArtistTrack(artist, t.getName()) +
                                                                                          ")" + " - " + t.getPlays() + CommandUtil.singlePlural(t.getPlays(), " play", " plays") + "\n").toList();
                    for (int i = 0; i < fullAlbumEntity.getTrackList().size() && i <= 20; i++) {
                        String s = lines.get(i);
                        a.append(i + 1).append(s);
                    }
                    EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                            .setDescription(a)
                            .setTitle(String.format("%s tracklist", album), LinkUtils.getLastFmArtistAlbumUrl(artist, album))
                            .setFooter(String.format("%s has %d total plays on the album!!%n", CommandUtil.unescapedUser(getUserString(e, params.getLastFMData().getDiscordId()), params.getLastFMData().getDiscordId(), e), fullAlbumEntity.getTotalPlayNumber()), null)
                            .setThumbnail(fullAlbumEntity.getAlbumUrl());
                    e.sendMessage(embedBuilder.build()).queue(message ->
                            new Reactionary<>(lines, message, 20, embedBuilder));
                }
            }
        }
    }


}
