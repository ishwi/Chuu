package core.commands.stats;

import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.services.SpotifyTrackService;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.ScrobbledTrack;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AudioFeaturesCommand extends ConcurrentCommand<ChuuDataParams> {
    private final Spotify spotify;

    public AudioFeaturesCommand(ChuuService dao) {
        super(dao);
        this.spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(getService());
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
    protected void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {
        LastFMData lastFMData = params.getLastFMData();
        SpotifyTrackService spotifyTrackService = new SpotifyTrackService(getService(), lastFMData.getName());
        List<ScrobbledTrack> tracksWithId = spotifyTrackService.getTracksWithId();
        List<AudioFeatures> audioFeatures = spotify.getAudioFeatures(tracksWithId.stream().map(ScrobbledTrack::getSpotifyId).collect(Collectors.toSet()));
        CompletableFuture.runAsync(() -> {
            var audioFeaturesStream = audioFeatures.stream().map(t ->
                    new dao.entities.AudioFeatures(t.getAcousticness(), t.getAnalysisUrl(), t.getDanceability(), t.getDurationMs(), t.getEnergy(), t.getId(), t.getInstrumentalness(), t.getKey(), t.getLiveness(), t.getLoudness(), t.getSpeechiness(), t.getTempo(), t.getTimeSignature(), t.getTrackHref(), t.getUri(), t.getValence())).collect(Collectors.toList());
            getService().insertAudioFeatures(audioFeaturesStream);
        });
        Optional<AudioFeatures> reduce = audioFeatures.stream().reduce((a, b) ->
                a.builder().setAcousticness(a.getAcousticness() + b.getAcousticness())
                        .setDanceability(a.getDanceability() + b.getDanceability())
                        .setDurationMs(a.getDurationMs() + b.getDurationMs())
                        .setEnergy(a.getEnergy() + b.getEnergy())
                        .setInstrumentalness(a.getInstrumentalness() + b.getInstrumentalness())
                        .setKey(a.getKey() + b.getKey())
                        .setLiveness(a.getLiveness() + b.getLiveness())
                        .setLoudness((float) (Math.pow(10, (a.getLoudness() + 60) / 10.0) + Math.pow(10, (a.getLoudness() + 60) / 10.0)))
                        .setSpeechiness(a.getSpeechiness() + b.getSpeechiness())
                        .setTempo(a.getTempo() + b.getTempo())
                        .setValence(a.getValence() + b.getValence())
                        .build());
        double[] doubles = audioFeatures.stream().mapToDouble(AudioFeatures::getLoudness).toArray();
        OptionalDouble average = Arrays.stream(doubles).map(x -> Math.pow(10, (x + 60) / 10.0)).average();
        if (reduce.isEmpty()) {
            sendMessageQueue(e, "Couldn't find any audio feature in your tracks");
            return;
        }
        DecimalFormat df = new DecimalFormat("##.##%");
        DecimalFormat db = new DecimalFormat("##.# 'dB' ");

        int s = audioFeatures.size();
        DiscordUserDisplay userInfoNotStripped = CommandUtil.getUserInfoNotStripped(e, params.getLastFMData().getDiscordId());
        AudioFeatures audioFeature = reduce.get();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Audio features for " + userInfoNotStripped.getUsername(), PrivacyUtils.getLastFmUser(lastFMData.getName()), userInfoNotStripped.getUrlImage())
                .setColor(CommandUtil.randomColor())
                .addField("Happiness:", df.format(audioFeature.getValence() / s), true)
                .addField("Acousticness:", df.format(audioFeature.getAcousticness() / s), true)
                .addField("Danceability:", df.format(audioFeature.getDanceability() / s), true)
                .addField("Instrumentalness:", df.format(audioFeature.getInstrumentalness() / s), true)
                .addField("Liveness:", df.format(audioFeature.getLiveness() / s), true);
        if (average.isPresent()) {
            embedBuilder.addField("Loudness:", db.format(10 * Math.log10(average.getAsDouble())), true);

        }
        embedBuilder.addField("Energy:", df.format(audioFeature.getEnergy() / s), true)
                .addField("Average Tempo:", (int) (audioFeature.getTempo() / s) + " BPM", true);

        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
