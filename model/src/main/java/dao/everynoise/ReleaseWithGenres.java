package dao.everynoise;

import java.util.Set;

public record ReleaseWithGenres(String artist, String release, String href, Set<String> genre) {
}
