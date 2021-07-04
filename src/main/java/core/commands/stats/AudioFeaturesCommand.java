package core.commands.stats;

import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.services.SpotifyTrackService;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.ScrobbledTrack;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AudioFeaturesCommand extends ConcurrentCommand<ChuuDataParams> {
    private final Spotify spotify;

    public AudioFeaturesCommand(ServiceView dao) {
        super(dao);
        this.spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
    }

    @Override
    public String getDescription() {
        return "Gets your audio features using Spotify data";
    }

    @Override
    public List<String> getAliases() {
        return List.of("audio", "audiofeatures");
    }

    @Override
    public String getName() {
        return "Audio Features";
    }

    @Override
    protected void onCommand(Context e, @NotNull ChuuDataParams params) {
        LastFMData lastFMData = params.getLastFMData();

        CompletableFuture<Void> cF = CompletableFuture.runAsync(() -> {
            SpotifyTrackService spotifyTrackService = new SpotifyTrackService(db, lastFMData.getName());
            List<ScrobbledTrack> tracksWithId = spotifyTrackService.getTracksWithId();
            List<AudioFeatures> audioFeatures = spotify.getAudioFeatures(tracksWithId.stream().map(ScrobbledTrack::getSpotifyId).collect(Collectors.toSet()));
            var audioFeaturesStream = audioFeatures.stream().map(t ->
                    new dao.entities.AudioFeatures(t.getAcousticness(), t.getAnalysisUrl(), t.getDanceability(), t.getDurationMs(), t.getEnergy(), t.getId(), t.getInstrumentalness(), t.getKey(), t.getLiveness(), t.getLoudness(), t.getSpeechiness(), t.getTempo(), t.getTimeSignature(), t.getTrackHref(), t.getUri(), t.getValence())).toList();
            db.insertAudioFeatures(audioFeaturesStream);
        });
        dao.entities.AudioFeatures userFeatures = db.getUserFeatures(lastFMData.getName());

        if (userFeatures == null) {
            cF.join();
            userFeatures = db.getUserFeatures(lastFMData.getName());
            if (userFeatures == null) {
                sendMessageQueue(e, "Couldn't find any audio feature in your tracks");
                return;
            }
        }
        DecimalFormat df = new DecimalFormat("##.##%");
        DecimalFormat db = new DecimalFormat("##.# 'dB' ");

        DiscordUserDisplay userInfoNotStripped = CommandUtil.getUserInfoUnescaped(e, params.getLastFMData().getDiscordId());
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor("Audio features for " + userInfoNotStripped.username(), PrivacyUtils.getLastFmUser(lastFMData.getName()), userInfoNotStripped.urlImage())
                .addField("Happiness:", df.format(userFeatures.valence()), true)
                .addField("Acousticness:", df.format(userFeatures.acousticness()), true)
                .addField("Danceability:", df.format(userFeatures.danceability()), true)
                .addField("Instrumentalness:", df.format(userFeatures.instrumentalness()), true)
                .addField("Liveness:", df.format(userFeatures.liveness()), true)
                .addField("Loudness:", db.format(userFeatures.loudness()), true)
                .addField("Energy:", df.format(userFeatures.energy()), true)
                .addField("Average Tempo:", Math.round(userFeatures.tempo()) + " BPM", true)
                .addField("Average song length", CommandUtil.getTimestamp(userFeatures.durationMs()), true);
        if (CommandUtil.rand.nextFloat() > 0.92f) {
            embedBuilder.setFooter("Data comes from Spotify");
        }


        e.sendMessage(embedBuilder.build()).queue();
    }
}
