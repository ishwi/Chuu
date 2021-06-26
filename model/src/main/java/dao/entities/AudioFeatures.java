package dao.entities;

import java.util.List;

public record AudioFeatures(Float acousticness, String analysisUrl, Float danceability,
                            Integer durationMs, Float energy, String id,
                            Float instrumentalness, Integer key, Float liveness,
                            List<Float> loudness, Float speechiness, Float tempo,
                            Integer timeSignature, String trackHref, String uri,
                            Float valence) {

    public AudioFeatures(Float acousticness, String analysisUrl, Float danceability, Integer durationMs, Float energy, String id, Float instrumentalness, Integer key, Float liveness, Float loudness, Float speechiness, Float tempo, Integer timeSignature, String trackHref, String uri, Float valence) {
        this(acousticness, analysisUrl, danceability, durationMs, energy, id, instrumentalness, key, liveness, List.of(loudness), speechiness, tempo, timeSignature, trackHref, uri, valence);
    }

    public AudioFeatures combine(AudioFeatures b) {
        var a = this;
        loudness.addAll(b.loudness);
        return new AudioFeatures(a.acousticness + b.acousticness, null,
                a.danceability + b.danceability,
                null, energy + b.energy,
                null, instrumentalness + b.instrumentalness,
                a.key + b.key,
                a.liveness + b.liveness,
                loudness,
                a.speechiness + b.speechiness,
                tempo + b.tempo, a.timeSignature + b.timeSignature, null, null,
                a.valence + b.valence);
    }

    public AudioFeatures flatten(int i) {
        double v = 10 * Math.log10(loudness.stream().mapToDouble(x -> Math.pow(10, (x + 60) / 10.0)).average().orElse(0));
        return new AudioFeatures(acousticness / i, null,
                danceability / i,
                null, energy / i,
                null, instrumentalness / i,
                key / i,
                liveness / i,
                List.of(((float) (v))),
                speechiness / i,
                tempo / i, timeSignature / i, null, null,
                valence / i);
    }
}
