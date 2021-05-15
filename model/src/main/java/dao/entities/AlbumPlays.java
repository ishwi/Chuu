package dao.entities;

import dao.utils.LinkUtils;

public class AlbumPlays extends ArtistPlays {
    private final String album;

    public AlbumPlays(String name, int countA, String album) {
        super(name, countA);
        this.album = album;
    }

    @Override
    public String toString() {
        return ". [" +
               LinkUtils.cleanMarkdownCharacter(getArtistName() + " - " + album) +
               "](" + LinkUtils.getLastFmArtistAlbumUrl(getArtistName(), album) +
               ") - " + getCount() +
               " plays\n";
    }
}
