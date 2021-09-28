package dao.entities;

public record CoverItem(String album, String artist, long albumId) {
    public CoverItem(long albumId) {
        this(null, null, albumId);
    }

    public CoverItem(String album, String artist) {
        this(album, artist, -1L);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoverItem coverItem = (CoverItem) o;

        if (!album.equalsIgnoreCase(coverItem.album)) return false;
        return artist.equalsIgnoreCase(coverItem.artist);
    }

    @Override
    public int hashCode() {
        int result = album != null ? album.toLowerCase().hashCode() : 0;
        result = 31 * result + (artist != null ? artist.toLowerCase().hashCode() : 0);
        return result;
        //-787712755
        //1283709359
    }
}
