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
import core.parsers.DaoParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.utils.Optionals;
import core.services.tracklist.GlobalTracklistService;
import core.services.tracklist.TracklistService;
import core.services.validators.AlbumValidator;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AlbumTracksGlobalDistributionCommand extends AlbumPlaysCommand {
    private final IPieableList<Track, ArtistAlbumParameters> pie;

    public AlbumTracksGlobalDistributionCommand(ServiceView dao) {
        super(dao);
        this.pie = new PieableListTrack(this.getParser());
        DaoParser<?> parser = (DaoParser<?>) this.getParser();
        parser.setAllowUnaothorizedUsers(true);
        parser.setExpensiveSearch(true);

    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public String getDescription() {
        return "Plays on each track of the provided album by the whole bot";
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        ArtistAlbumParser parser = new ArtistAlbumParser(db, lastFM, false, Optionals.LIST.opt, Optionals.PLAYS.opt);
        parser.setExpensiveSearch(true);
        return parser;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("globaltracks", "gtl");
    }

    @Override
    public String slashName() {
        return "tracklist";
    }

    @Override
    public String getName() {
        return "Global tracklist";
    }

    @Override
    protected void doSomethingWithAlbumArtist(ScrobbledArtist scrobbledArtist, String album, Context e, long who, ArtistAlbumParameters params) throws LastFmException {

        String artist = scrobbledArtist.getArtist();
        ScrobbledAlbum scrobbledAlbum = new AlbumValidator(db, lastFM).validate(scrobbledArtist.getArtistId(), artist, album);

        TracklistService tracklistService = new GlobalTracklistService(db);

        Optional<FullAlbumEntity> trackList = tracklistService.getTrackList(scrobbledAlbum, params.getLastFMData(), scrobbledArtist.getUrl(), e);
        if (trackList.isEmpty()) {
            sendMessageQueue(e, "Couldn't find a tracklist for " + CommandUtil.escapeMarkdown(scrobbledArtist.getArtist()
            ) + " - " + CommandUtil.escapeMarkdown(scrobbledAlbum.getAlbum()));
        } else {

            FullAlbumEntity fullAlbumEntity = trackList.get();
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
                    pieChart.setTitle(params.getArtist() + " - " + params.getAlbum() + " tracklist in " + e.getJDA().getSelfUser().getName());
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
                            .setFooter(String.format("%s has %d total plays on the album!!%n", e.getJDA().getSelfUser().getName(), fullAlbumEntity.getTrackList().stream().mapToInt(Track::getPlays).sum()), null)
                            .setThumbnail(fullAlbumEntity.getAlbumUrl());
                    e.sendMessage(embedBuilder.build()).queue(message ->
                            new Reactionary<>(lines, message, 20, embedBuilder));
                }
            }
        }
    }


}
