package dao.entities;

import dao.utils.LinkUtils;

public class TrackPlays extends ArtistPlays {
    private final String track;

    public TrackPlays(String name, int countA, String track) {
        super(name, countA);
        this.track = track;
    }

    @Override
    public String toString() {
        return ". [" +
                LinkUtils.cleanMarkdownCharacter(getArtistName() + " - " + track) +
                "](" + LinkUtils.getLastFMArtistTrack(getArtistName(), track) +
                ") - " + getCount() +
                " plays\n";
    }
}
