package dao.entities;

import java.util.regex.Pattern;

public class Genre {
    private static Pattern regex = Pattern.compile("(?:(?: and )|[ _&/-])");
    private String genreName;
    private String representativeArtist;

    public Genre(String genreName, String representativeArtist) {
        this.genreName = genreName;
        this.representativeArtist = representativeArtist;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getRepresentativeArtist() {
        return representativeArtist;
    }

    public void setRepresentativeArtist(String representativeArtist) {
        this.representativeArtist = representativeArtist;
    }

    @Override
    public int hashCode() {
        int result = mapString(genreName).hashCode();
        result = 31 * result + representativeArtist.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Genre genre = (Genre) o;

        if (!mapString(genreName).equals(mapString(genre.genreName))) return false;
        return representativeArtist.equals(genre.representativeArtist);
    }

    private String mapString(String string) {
        return regex.matcher(string).replaceAll("");
    }

}
