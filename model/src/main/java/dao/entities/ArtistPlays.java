package dao.entities;

import dao.utils.LinkUtils;

public class ArtistPlays {
    private final int count;
    private final String artistName;

    public ArtistPlays(String name, int countA) {
        this.artistName = name;
        this.count = countA;
    }

    @Override
    public String toString() {
        return ". [" +
                LinkUtils.cleanMarkdownCharacter(getArtistName()) +
                "](" + LinkUtils.getLastFmArtistUrl(artistName) +
                ") - " + getCount() +
                " plays\n";
    }

    public String getArtistName() {
        return artistName;
    }

    public int getCount() {
        return count;
    }
}
