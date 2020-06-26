package dao.entities;

import core.commands.CommandUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ScoredAlbumRatings {
    private final double score;
    private final String name;
    private final String url;
    private final long numberOfRatings;
    private final double average;
    private String artist;
    public final static NumberFormat formatter = new DecimalFormat("#0.##");


    public ScoredAlbumRatings(double score, String name, String url, long numberOfRatings, double average, String artist) {
        this.score = score;
        this.name = name;
        this.url = url;
        this.numberOfRatings = numberOfRatings;
        this.average = average;
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public double getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public long getNumberOfRatings() {
        return numberOfRatings;
    }

    public double getAverage() {
        return average;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String toString() {
        return ". ***[" + CommandUtil.cleanMarkdownCharacter(getArtist()) + " - " + CommandUtil.cleanMarkdownCharacter(getName())
                +
                "](" + CommandUtil.getLastFmArtistAlbumUrl(getArtist(), getName()) +
                ")***\n\t" + String.format("Average: **%s** | # of Ratings: **%d**", formatter.format(getAverage() / 2f), getNumberOfRatings()) +
                "\n";
    }
}
