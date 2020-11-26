package dao.entities;

public class AudioFeatures {
    private final Float acousticness;
    private final String analysisUrl;
    private final Float danceability;
    private final Integer durationMs;
    private final Float energy;
    private final String id;
    private final Float instrumentalness;
    private final Integer key;
    private final Float liveness;
    private final Float loudness;
    private final Float speechiness;
    private final Float tempo;
    private final Integer timeSignature;
    private final String trackHref;
    private final String uri;
    private final Float valence;

    public AudioFeatures(Float acousticness, String analysisUrl, Float danceability, Integer durationMs, Float energy, String id, Float instrumentalness, Integer key, Float liveness, Float loudness, Float speechiness, Float tempo, Integer timeSignature, String trackHref, String uri, Float valence) {
        this.acousticness = acousticness;
        this.analysisUrl = analysisUrl;
        this.danceability = danceability;
        this.durationMs = durationMs;
        this.energy = energy;
        this.id = id;
        this.instrumentalness = instrumentalness;
        this.key = key;
        this.liveness = liveness;
        this.loudness = loudness;
        this.speechiness = speechiness;
        this.tempo = tempo;
        this.timeSignature = timeSignature;
        this.trackHref = trackHref;
        this.uri = uri;
        this.valence = valence;
    }

    public Float getAcousticness() {
        return acousticness;
    }

    public String getAnalysisUrl() {
        return analysisUrl;
    }

    public Float getDanceability() {
        return danceability;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public Float getEnergy() {
        return energy;
    }

    public String getId() {
        return id;
    }

    public Float getInstrumentalness() {
        return instrumentalness;
    }

    public Integer getKey() {
        return key;
    }

    public Float getLiveness() {
        return liveness;
    }

    public Float getLoudness() {
        return loudness;
    }

    public Float getSpeechiness() {
        return speechiness;
    }

    public Float getTempo() {
        return tempo;
    }

    public Integer getTimeSignature() {
        return timeSignature;
    }

    public String getTrackHref() {
        return trackHref;
    }

    public String getUri() {
        return uri;
    }

    public Float getValence() {
        return valence;
    }
}
