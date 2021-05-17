package dao.entities;

import dao.utils.LinkUtils;

public class StolenCrown {
    private final String artist;
    private final int ogPlays;
    private final int queriedPlays;

    public StolenCrown(String artist, int ogPlays, int queriedPlays) {
        this.artist = artist;
        this.ogPlays = ogPlays;
        this.queriedPlays = queriedPlays;
    }

    public String getArtist() {
        return artist;
    }

    public int getOgPlays() {
        return ogPlays;
    }

    public int getQueriedPlays() {
        return queriedPlays;
    }

    @Override
    public String toString() {
        return ". [" +
               LinkUtils.cleanMarkdownCharacter(artist) +
               "](" + LinkUtils.getLastFmArtistUrl(artist) +
               ") : " + ogPlays +
               " \u279C " + queriedPlays + "\n";
    }
}
