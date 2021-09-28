package dao.entities;

import java.util.regex.Pattern;

public class Genre {
    private static final Pattern regex = Pattern.compile(" and |[ _&/-]", Pattern.CASE_INSENSITIVE);
    private String name;

    public Genre(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public int hashCode() {
        //result = 31 * result + representativeArtist.hashCode();
        return mapString(name).toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Genre genre = (Genre) o;

        return mapString(name).equalsIgnoreCase(mapString(genre.name));
    }

    public String mapString(String string) {
        return regex.matcher(string).replaceAll("");
    }

}
