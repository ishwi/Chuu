package dao.entities;

public class ArtistMusicBrainzDetails {
    private final String gender;
    private final String countryCode;

    public ArtistMusicBrainzDetails(String gender, String countryCode) {
        this.gender = gender;
        this.countryCode = countryCode;
    }

    public String getGender() {
        return gender;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
