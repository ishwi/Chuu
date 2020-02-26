package dao.entities;

public class UpdaterStatus {
    private final boolean correction_status;
    private String artistUrl;
    private long artistId;

    //Url -> null
    public UpdaterStatus(String url, boolean correction_status, long artistId) {
        this.artistUrl = url;
        this.correction_status = correction_status;
        this.artistId = artistId;
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


    public boolean isCorrection_status() {
        return correction_status;
    }

}
