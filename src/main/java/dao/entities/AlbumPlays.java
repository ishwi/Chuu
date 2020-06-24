package dao.entities;

import core.commands.CommandUtil;

public class AlbumPlays extends ArtistPlays {
    private final String album;

    public AlbumPlays(String name, int countA, String album) {
        super(name, countA);
        this.album = album;
    }

    @Override
    public String toString() {
        return ". [" +
                CommandUtil.cleanMarkdownCharacter(getArtistName() + " - " + album) +
                "](" + CommandUtil.getLastFmArtistAlbumUrl(getArtistName(), album) +
                ") - " + getCount() +
                " plays\n";
    }
}
