package dao.entities;

import dao.utils.LinkUtils;

public class TagPlays {
    private final int count;
    private final String tag;

    public TagPlays(String name, int countA) {
        this.tag = name;
        this.count = countA;
    }

    @Override
    public String toString() {
        return ". [" +
               LinkUtils.cleanMarkdownCharacter(getTag()) +
               "](" + LinkUtils.getLastFmArtistUrl(tag) +
               ") - " + getCount() +
               " plays\n";
    }

    public String getTag() {
        return tag;
    }

    public int getCount() {
        return count;
    }
}
