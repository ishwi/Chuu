package core.commands.stats;

import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.Track;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.services.SpotifyTrackService;
import core.services.validators.ArtistValidator;
import core.services.validators.TrackValidator;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.ScrobbledTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SongAudioFeaturesCommand extends ConcurrentCommand<ArtistAlbumParameters> {
    private final Spotify spotify;

    public SongAudioFeaturesCommand(ServiceView dao) {
        super(dao);
        this.spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.INFO;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistSongParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Gets audio features of a specific song using Spotify data";
    }

    @Override
    public List<String> getAliases() {
        return List.of("trackfeatures", "songfeatures", "songft", "trackft");
    }

    @Override
    public String getName() {
        return "Specific song features";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistAlbumParameters params) throws LastFmException {
        LastFMData lastFMData = params.getLastFMData();

        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), false, true);
        long trackId = new TrackValidator(db, lastFM).validate(sA.getArtistId(), params.getArtist(), params.getAlbum()).getTrackId();

        ScrobbledTrack scrobbledTrack = new ScrobbledTrack(
                params.getArtist(), params.getAlbum(), 1, false, 0, null, null, null);
        scrobbledTrack.setTrackId(trackId);


        SpotifyTrackService spotifyTrackService = new SpotifyTrackService(db, lastFMData.getName());
        List<Pair<ScrobbledTrack, com.wrapper.spotify.model_objects.specification.Track>> pairs = spotifyTrackService.searchTracks(List.of(scrobbledTrack));

        if (pairs.isEmpty() || pairs.get(0).getValue() == null) {
            sendMessageQueue(e, "Couldn't find any audio feature for **%s by %s**".formatted(params.getAlbum(), sA.getArtist()));
            return;
        }
        Track value = pairs.get(0).getValue();
        List<AudioFeatures> audioFeatures = spotify.getAudioFeatures(Set.of(value.getId()));
        CompletableFuture.runAsync(() -> {
            var audioFeaturesStream = audioFeatures.stream().map(t ->
                    new dao.entities.AudioFeatures(t.getAcousticness(), t.getAnalysisUrl(), t.getDanceability(), t.getDurationMs(), t.getEnergy(), t.getId(), t.getInstrumentalness(), t.getKey(), t.getLiveness(), t.getLoudness(), t.getSpeechiness(), t.getTempo(), t.getTimeSignature(), t.getTrackHref(), t.getUri(), t.getValence())).toList();
            db.insertAudioFeatures(audioFeaturesStream);
        });


        if (audioFeatures.isEmpty()) {
            sendMessageQueue(e, "Couldn't find any audio feature for **%s by %s**".formatted(sA.getArtist(), params.getAlbum()));
            return;
        }
        DecimalFormat df = new DecimalFormat("##.##%");
        DecimalFormat db = new DecimalFormat("##.# 'dB' ");

        int s = audioFeatures.size();
        DiscordUserDisplay userInfoNotStripped = CommandUtil.getUserInfoUnescaped(e, params.getLastFMData().getDiscordId());
        AudioFeatures audioFeature = audioFeatures.get(0);
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor("Audio features for " + scrobbledTrack.getName() + " by " + scrobbledTrack.getArtist(), PrivacyUtils.getLastFmUser(lastFMData.getName()), userInfoNotStripped.urlImage())
                .addField("Happiness:", df.format(audioFeature.getValence() / s), true)
                .addField("Acousticness:", df.format(audioFeature.getAcousticness() / s), true)
                .addField("Danceability:", df.format(audioFeature.getDanceability() / s), true)
                .addField("Instrumentalness:", df.format(audioFeature.getInstrumentalness() / s), true)
                .addField("Liveness:", df.format(audioFeature.getLiveness() / s), true)
                .addField("Loudness:", db.format(audioFeature.getLoudness() + 60), true)
                .addField("Energy:", df.format(audioFeature.getEnergy() / s), true)
                .addField("Average Tempo:", (int) (audioFeature.getTempo() / s) + " BPM", true)
                .addField("Length:", CommandUtil.getTimestamp(audioFeature.getDurationMs()), true);
        if (CommandUtil.rand.nextFloat() > 0.92f) {
            embedBuilder.setFooter("Data comes from Spotify");
        }

        e.sendMessage(embedBuilder.build()).queue();
    }
}
