package dao.everynoise;

import java.util.Set;

public record ReleaseOfGenre(String artist, String release, String href, Set<String> genre) {
}
