package dao.everynoise;

import java.util.List;

public record NoiseGenreReleases(String name, String uri, List<Release> releases) {
}
