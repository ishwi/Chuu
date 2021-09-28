package dao.entities;

public class ArtistInfo extends EntityInfo {
    private String artistUrl;


    public ArtistInfo(String artistUrl, String artistName) {
        super(null, artistName);
        this.artistUrl = artistUrl;
    }

    public ArtistInfo(String artistUrl, String artistName, String mbid) {
        super(mbid, artistName);
        this.artistUrl = artistUrl;
    }


    public String getArtistUrl() {
        return artistUrl;
    }

    public void setArtistUrl(String artistUrl) {
        this.artistUrl = artistUrl;
    }

}
