package core.music.sources.youtube.webscrobbler.processers;

public record Processed(String artist, String album, String song, long msStart) {


    public Processed(String artist, String album, String song) {
        this(artist, album, song, 0);
    }

}
