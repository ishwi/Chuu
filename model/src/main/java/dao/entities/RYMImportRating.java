package dao.entities;

import java.time.Year;

public class RYMImportRating {
    private final long RYMid;
    private final String firstName;
    private final String lastName;
    private final String firstNameLocalized;
    private final String lastNameLocalized;
    private final String title;
    private final Year year;
    private final Byte rating;
    private final boolean ownership;
    private final Year purchaseDate;
    private final String mediaType;
    private final String review;
    private long id = -1;
    private long artist_id = -1;
    private String realAlbumName = null;

    public RYMImportRating(long RYMid, String firstName, String lastName, String firstNameLocalized, String lastNameLocalized, String title, Year year, Byte rating, boolean ownership, Year purchaseDate, String mediaType, String review) {
        this.RYMid = RYMid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.firstNameLocalized = firstNameLocalized;
        this.lastNameLocalized = lastNameLocalized;
        this.title = title;
        this.year = year;
        this.rating = rating;
        this.ownership = ownership;
        this.purchaseDate = purchaseDate;
        this.mediaType = mediaType;
        this.review = review;
    }

    public long getRYMid() {
        return RYMid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstNameLocalized() {
        return firstNameLocalized;
    }

    public String getLastNameLocalized() {
        return lastNameLocalized;
    }

    public String getTitle() {
        return title;
    }

    public Year getYear() {
        return year;
    }

    public Byte getRating() {
        return rating;
    }

    public boolean isOwnership() {
        return ownership;
    }

    public Year getPurchaseDate() {
        return purchaseDate;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getReview() {
        return review;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(long artist_id) {
        this.artist_id = artist_id;
    }

    public String getRealAlbumName() {
        return realAlbumName;
    }

    public void setRealAlbumName(String realAlbumName) {
        this.realAlbumName = realAlbumName;
    }

    public String getNormalizedAlbumName() {
        return this.title;
    }
}
