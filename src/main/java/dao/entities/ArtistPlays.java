package dao.entities;

import core.commands.CommandUtil;

public class ArtistPlays {
    private final int count;
    private final String artistName;

    public ArtistPlays(String name, int count_a) {
        this.artistName = name;
        this.count = count_a;
    }

    @Override
    public String toString() {
        return ". [" +
               CommandUtil.cleanMarkdownCharacter(getArtistName()) +
               "](" + CommandUtil.getLastFmArtistUrl(artistName) +
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
