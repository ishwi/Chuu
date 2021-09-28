package dao.entities;

public record AudioFeatures(Float acousticness, String analysisUrl, Float danceability,
                            Integer durationMs, Float energy, String id,
                            Float instrumentalness, Integer key, Float liveness,
                            Float loudness, Float speechiness, Float tempo,
                            Integer timeSignature, String trackHref, String uri,
                            Float valence) {



}
