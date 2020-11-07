package core.commands;

import com.google.common.collect.Multimaps;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.TrackDistributor;
import core.imagerenderer.util.IPieableList;
import core.imagerenderer.util.PieableListTrack;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistAlbumParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.FullAlbumEntity;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.Track;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.imgscalr.Scalr;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class AlbumTracksDistributionCommand extends AlbumPlaysCommand {
    private final MusicBrainzService mb;
    private final Spotify spotifyApi;
    private final IPieableList<Track, ArtistAlbumParameters> pie;

    public AlbumTracksDistributionCommand(ChuuService dao) {

        super(dao);
        mb = MusicBrainzServiceSingleton.getInstance();
        this.pie = new PieableListTrack(this.parser);
        spotifyApi = SpotifySingleton.getInstance();
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
        ArtistAlbumParser parser = new ArtistAlbumParser(getService(), lastFM);
        parser.addOptional(new OptionalEntity("list", "display in list format"));

        parser.setExpensiveSearch(true);
        return parser;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("tracks", "tt");
    }

    @Override
    public String getName() {
        return "Track Distribution";
    }

    @Override
    void doSomethingWithAlbumArtist(ScrobbledArtist scrobbledArtist, String album, MessageReceivedEvent e, long who, ArtistAlbumParameters params) throws LastFmException {

        FullAlbumEntity fullAlbumEntity;
        String artistUrl = scrobbledArtist.getUrl();
        String artist = scrobbledArtist.getArtist();
        long artistId = scrobbledArtist.getArtistId();
        LastFMData data = params.getLastFMData();
        try {
            fullAlbumEntity = lastFM.getTracksAlbum(data.getName(), artist, album);

        } catch (LastFmEntityNotFoundException ex)
        //If it doesnt exists on last.fm we do a little workaround
        {
            int artistPlays = getService().getArtistPlays(artistId, data.getName());
            fullAlbumEntity = new FullAlbumEntity(artist, album, artistPlays, null, data.getName());
        }
        Set<Track> trackList = new HashSet<>(fullAlbumEntity.getTrackList());

        if (trackList.isEmpty()) {
            List<Track> spoList = spotifyApi.getAlbumTrackList(artist, album);
            handleList(trackList, spoList, e, data.getName());
        }
        if (trackList.isEmpty()) {
            if (fullAlbumEntity.getMbid() != null && !fullAlbumEntity.getMbid().isBlank()) {
                List<Track> albumTrackListMbid = mb.getAlbumTrackListMbid(fullAlbumEntity.getMbid());
                handleList(trackList, albumTrackListMbid, e, data.getName());
            }
            if (trackList.isEmpty()) {
                List<Track> albumTrackList = mb.getAlbumTrackList(fullAlbumEntity.getArtist(), fullAlbumEntity.getAlbum());
                handleList(trackList, albumTrackList, e, data.getName());


                if (trackList.isEmpty()) {
                    //Force it to lowerCase
                    List<Track> albumTrackListLowerCase = mb.getAlbumTrackListLowerCase(fullAlbumEntity.getArtist(), fullAlbumEntity.getAlbum());
                    handleList(trackList, albumTrackListLowerCase, e, data.getName());
                    if (trackList.isEmpty()) {
                        //If is still empty well fuck it

                        sendMessageQueue(e, "Couldn't find a tracklist for " + CommandUtil.cleanMarkdownCharacter(fullAlbumEntity
                                .getArtist()) + " - " + CommandUtil.cleanMarkdownCharacter(fullAlbumEntity
                                .getAlbum()));
                        return;
                    }
                }

            }

        }
        fullAlbumEntity.setTrackList(trackList.stream().sorted(Comparator.comparingInt(Track::getPosition)).collect(Collectors.toList()));
        List<Track> handler = new ArrayList<>(trackList);

        List<Track> collect = Multimaps.index(handler, Track::getPosition)
                .asMap().values().stream()
                .map(value -> {
                    Optional<Track> max = value.stream().max(Comparator.comparingInt(Track::getPlays));
                    return max.orElse(null);
                }).filter(Objects::nonNull).sorted(Comparator.comparingInt(Track::getPosition))
                .collect(Collectors.toList());
        if (trackList.stream().mapToInt(Track::getPlays).sum() <= collect.stream().mapToInt(Track::getPlays).sum()) {
            fullAlbumEntity.setTrackList(collect);
        }

        fullAlbumEntity.setArtistUrl(artistUrl);

        switch (CommandUtil.getEffectiveMode(params.getLastFMData().getRemainingImagesMode(), params)) {

            case IMAGE:
                BufferedImage bufferedImage = TrackDistributor.drawImage(fullAlbumEntity);
                sendImage(bufferedImage, e);
                break;

            case PIE:
                PieChart pieChart = this.pie.doPie(params, fullAlbumEntity.getTrackList());
                pieChart.setTitle(params.getArtist() + " - " + params.getAlbum() + " tracklist");
                bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = bufferedImage.createGraphics();
                GraphicUtils.setQuality(g);

                pieChart.paint(g, 1000, 750);
                BufferedImage image = GraphicUtils.getImage(fullAlbumEntity.getAlbumUrl());
                if (image != null) {
                    BufferedImage backgroundImage = Scalr.resize(image, 150);
                    g.drawImage(backgroundImage, 10, 750 - 10 - backgroundImage.getHeight(), null);
                }
                sendImage(bufferedImage, params.getE());
                break;
            case LIST:
                StringBuilder a = new StringBuilder();
                List<String> collect1 = fullAlbumEntity.getTrackList().stream().map(t -> ". " + "[" +
                        CommandUtil.cleanMarkdownCharacter(t.getName()) +
                        "](" + LinkUtils.getLastFMArtistTrack(artist, t.getName()) +
                        ")" + " - " + t.getPlays() + CommandUtil.singlePlural(t.getPlays(), " play", " plays") + "\n").collect(Collectors.toList());
                for (int i = 0; i < collect.size() && i <= 20; i++) {
                    String s = collect1.get(i);
                    a.append(i + 1).append(s);
                }
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription(a)
                        .setColor(CommandUtil.randomColor())
                        .setTitle(String.format("%s tracklist", album), LinkUtils.getLastFmArtistAlbumUrl(artist, album))
                        .setFooter(String.format("%s has %d total plays on the album!!%n", CommandUtil.markdownLessUserString(getUserString(e, params.getLastFMData().getDiscordId()), params.getLastFMData().getDiscordId(), e), fullAlbumEntity.getTotalPlayNumber()), null)
                        .setThumbnail(fullAlbumEntity.getAlbumUrl());

                MessageBuilder mes = new MessageBuilder();
                e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message ->
                        new Reactionary<>(collect, message, 20, embedBuilder));
                break;
        }
    }

    private void handleList(Set<Track> trackList, List<Track> listToHandle, MessageReceivedEvent e, String lastfmName) {
        if (listToHandle.size() > 50) {
            sendMessageQueue(e, "Track list is too big for me to calculate all the plays");
            return;
        }
        listToHandle.stream().map(t ->
                {
                    try {
                        Track trackInfo = lastFM.getTrackInfo(lastfmName, t.getArtist(), t.getName());
                        trackInfo.setPosition(t.getPosition());
                        trackInfo.setName(t.getName());
                        return trackInfo;
                    } catch (LastFmException ex) {
                        return t;
                    }
                }
        ).forEach(trackList::add);
    }
}
