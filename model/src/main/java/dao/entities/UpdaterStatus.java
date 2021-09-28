package dao.entities;

public class UpdaterStatus {
    private final boolean correctionStatus;
    private String artistUrl;
    private long artistId;
    private String artistName;

    //Url -> null
    public UpdaterStatus(String url, boolean correctionStatus, long artistId, String artistName) {
        this.artistUrl = url;
        this.correctionStatus = correctionStatus;
        this.artistId = artistId;
        this.artistName = artistName;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public String getArtistUrl() {
        return artistUrl;
    }

    public void setArtistUrl(String artistUrl) {
        this.artistUrl = artistUrl;
    }


    public boolean isCorrectionStatus() {
        return correctionStatus;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
}
