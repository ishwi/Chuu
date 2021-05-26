package dao.entities;

public record AudioFeatures(Float acousticness, String analysisUrl, Float danceability,
                            Integer durationMs, Float energy, String id,
                            Float instrumentalness, Integer key, Float liveness,
                            Float loudness, Float speechiness, Float tempo,
                            Integer timeSignature, String trackHref, String uri,
                            Float valence) {
    public AudioFeatures combine(AudioFeatures b) {
        var a = this;
        return new AudioFeatures(a.acousticness + b.acousticness, null,
                a.danceability + b.danceability,
                null, energy + b.energy,
                null, instrumentalness + b.instrumentalness,
                a.key + b.key,
                a.liveness + b.liveness,
                (float) (Math.pow(10, (a.loudness() + 60) / 10.0) + Math.pow(10, (a.loudness() + 60) / 10.0)),
                a.speechiness + b.speechiness,
                tempo + b.tempo, a.timeSignature + b.timeSignature, null, null,
                a.valence + b.valence);
    }

}
