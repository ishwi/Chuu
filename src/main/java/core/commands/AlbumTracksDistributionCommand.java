package core.commands;

import com.google.common.collect.Multimaps;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.TrackDistributor;
import dao.ChuuService;
import dao.entities.FullAlbumEntity;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.Track;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

public class AlbumTracksDistributionCommand extends AlbumPlaysCommand {
    private final MusicBrainzService mb;
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public AlbumTracksDistributionCommand(ChuuService dao) {

        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
        mb = MusicBrainzServiceSingleton.getInstance();
    }

    @Override
    public String getDescription() {
        return "Plays on each track of the provided album";
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
    void doSomethingWithAlbumArtist(ScrobbledArtist scrobbledArtist, String album, MessageReceivedEvent e, long who) throws InstanceNotFoundException, LastFmException {

        FullAlbumEntity fullAlbumEntity;
        String artistUrl = scrobbledArtist.getUrl();
        String artist = scrobbledArtist.getArtist();
        long artistId = scrobbledArtist.getArtistId();
        LastFMData data = getService().findLastFMData(who);

        try {
            fullAlbumEntity = lastFM.getTracksAlbum(data.getName(), artist, album);

        } catch (LastFmEntityNotFoundException ex)
        //If it doesnt exists on last.fm we do a little workaround
        {
            int artistPlays = getService().getArtistPlays(artistId, data.getName());
            fullAlbumEntity = new FullAlbumEntity(artist, album, artistPlays, null, data.getName());
        }

        List<Track> trackList = fullAlbumEntity.getTrackList();

        if (trackList.isEmpty()) {
            if (fullAlbumEntity.getMbid() != null && !fullAlbumEntity.getMbid().isBlank()) {
                mb.getAlbumTrackListMbid(fullAlbumEntity.getMbid()).stream().map(t ->
                        {
                            try {
                                return lastFM.getTrackInfo(data.getName(), t.getArtist(), t.getName());
                            } catch (LastFmException ex) {
                                return t;
                            }
                        }
                ).sorted(Comparator.comparingInt(Track::getPosition)).forEach(fullAlbumEntity::addTrack);
            }
            if (trackList.isEmpty()) {
                mb.getAlbumTrackList(fullAlbumEntity.getArtist(), fullAlbumEntity.getAlbum())
                        .stream().map(t ->
                        {
                            try {
                                return lastFM.getTrackInfo(data.getName(), t.getArtist(), t.getName());
                            } catch (LastFmException ex) {
                                return t;
                            }
                        }
                ).sorted(Comparator.comparingInt(Track::getPosition)).forEach(fullAlbumEntity::addTrack);

                if (trackList.isEmpty()) {
                    //Force it to lowerCase
                    mb.getAlbumTrackListLowerCase(fullAlbumEntity.getArtist(), fullAlbumEntity.getAlbum())
                            .stream().map(t ->
                            {
                                try {
                                    return lastFM.getTrackInfo(data.getName(), t.getArtist(), t.getName());
                                } catch (LastFmException ex) {
                                    return t;
                                }
                            }
                    ).sorted(Comparator.comparingInt(Track::getPosition)).forEach(fullAlbumEntity::addTrack);

                    if (trackList.isEmpty()) {
                        //If is still empty well fuck it

                        sendMessageQueue(e, "Couldn't find a tracklist for " + fullAlbumEntity
                                .getArtist() + " - " + fullAlbumEntity
                                                    .getAlbum());
                        return;
                    }
                }

            }

        }
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
        BufferedImage bufferedImage = TrackDistributor.drawImage(fullAlbumEntity, false);
        sendImage(bufferedImage, e);
    }
}
