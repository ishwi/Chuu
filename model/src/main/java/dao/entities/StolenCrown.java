package dao.entities;

import dao.utils.LinkUtils;

public record StolenCrown(String artist, int ogPlays, int queriedPlays) {

    @Override
    public String toString() {
        return ". [" +
               LinkUtils.cleanMarkdownCharacter(artist) +
               "](" + LinkUtils.getLastFmArtistUrl(artist) +
               ") : " + ogPlays +
               " ➜ " + queriedPlays + "\n";
    }
}
