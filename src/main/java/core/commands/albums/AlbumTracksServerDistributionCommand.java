package core.commands.albums;

import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.TrackDistributor;
import core.imagerenderer.util.IPieableList;
import core.imagerenderer.util.PieableListTrack;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistAlbumParser;
import core.parsers.DaoParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.services.ColorService;
import core.services.tracklist.ServerTracklistService;
import core.services.tracklist.TracklistService;
import dao.ChuuService;
import dao.entities.FullAlbumEntity;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;
import dao.entities.Track;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.imgscalr.Scalr;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AlbumTracksServerDistributionCommand extends AlbumPlaysCommand {
    private final IPieableList<Track, ArtistAlbumParameters> pie;

    public AlbumTracksServerDistributionCommand(ChuuService dao) {
        super(dao);
        this.pie = new PieableListTrack(this.getParser());
        DaoParser<?> parser = (DaoParser<?>) this.getParser();
        parser.setAllowUnaothorizedUsers(true);
        parser.setExpensiveSearch(true);

    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public String getDescription() {
        return "Plays on each track of the provided album by the whole server";
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
        return Arrays.asList("servertracks", "stl");
    }

    @Override
    public String getName() {
        return "Server Track Distribution";
    }

    @Override
    protected void doSomethingWithAlbumArtist(ScrobbledArtist scrobbledArtist, String album, MessageReceivedEvent e, long who, ArtistAlbumParameters params) throws LastFmException {

        String artist = scrobbledArtist.getArtist();
        ScrobbledAlbum scrobbledAlbum = CommandUtil.validateAlbum(db, scrobbledArtist.getArtistId(), album, lastFM);
        scrobbledAlbum.setArtist(scrobbledArtist.getArtist());
        TracklistService tracklistService = new ServerTracklistService(db, e.getGuild().getIdLong());

        Optional<FullAlbumEntity> trackList = tracklistService.getTrackList(scrobbledAlbum, params.getLastFMData(), scrobbledArtist.getUrl());
        if (trackList.isEmpty()) {
            sendMessageQueue(e, "Couldn't find a tracklist for " + CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist()
            ) + " - " + CommandUtil.cleanMarkdownCharacter(scrobbledAlbum.getAlbum()));
        } else {

            FullAlbumEntity fullAlbumEntity = trackList.get();

            switch (CommandUtil.getEffectiveMode(params.getLastFMData().getRemainingImagesMode(), params)) {
                case IMAGE -> {
                    BufferedImage bufferedImage = TrackDistributor.drawImage(fullAlbumEntity);
                    sendImage(bufferedImage, e);
                }
                case PIE -> {
                    PieChart pieChart = this.pie.doPie(params, fullAlbumEntity.getTrackList());
                    pieChart.setTitle(params.getArtist() + " - " + params.getAlbum() + " tracklist in " + e.getGuild().getName());
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
                    List<String> collect1 = fullAlbumEntity.getTrackList().stream().map(t -> ". " + "[" +
                            CommandUtil.cleanMarkdownCharacter(t.getName()) +
                            "](" + LinkUtils.getLastFMArtistTrack(artist, t.getName()) +
                            ")" + " - " + t.getPlays() + CommandUtil.singlePlural(t.getPlays(), " play", " plays") + "\n").collect(Collectors.toList());
                    for (int i = 0; i < fullAlbumEntity.getTrackList().size() && i <= 20; i++) {
                        String s = collect1.get(i);
                        a.append(i + 1).append(s);
                    }
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setDescription(a)
                            .setColor(ColorService.computeColor(e))
                            .setTitle(String.format("%s tracklist", album), LinkUtils.getLastFmArtistAlbumUrl(artist, album))
                            .setFooter(String.format("%s has %d total plays on the album!!%n", e.getGuild().getName(), fullAlbumEntity.getTrackList().stream().mapToInt(Track::getPlays).sum()), null)
                            .setThumbnail(fullAlbumEntity.getAlbumUrl());
                    e.getChannel().sendMessage(embedBuilder.build()).queue(message ->
                            new Reactionary<>(fullAlbumEntity.getTrackList(), message, 20, embedBuilder));
                }
            }
        }
    }


}
