package core.music.everynoise;

import core.apis.lyrics.TextSplitter;
import core.util.VirtualParallel;
import dao.everynoise.NoiseGenre;
import dao.everynoise.ReleaseWithGenres;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EveryNoiseScrapper {

    private final static String GENRE_LIST = "http://everynoise.com/new_releases_by_genre.cgi?region=US&albumsonly=&style=cards&date=&genre=anygenre&artistsfrom=";
    private final static String GENRE_URL = "https://everynoise.com/new_releases_by_genre.cgi?genre=%s&region=GB&albumsonly=true&hidedupes=off&date=%s";
    private final static String GENRE_PLAYLIST = "https://everynoise.com/everynoise1d.cgi?scope=all";
    private final static int URL_LENGTH = 8068;
    private final static int length = "https://embed.spotify.com/?uri=spotify:playlist:".length();
    private final static int length_album = "spotify:album:".length();

    public static Stream<? extends Item> albumToItem(Document genrePage) {
        List<Element> items = genrePage.select(".genrename").stream().toList();
        return VirtualParallel.runIO(items,
                z -> {
                    String genre = z.child(0).html();
                    return z.nextElementSibling().select(".albumbox").stream().map(album -> {
                        Element b = album.selectFirst("b");
                        Element i = album.selectFirst("i");
                        String artist = b.html();
                        String release = i.html();
                        String href = i.parent().attr("href").substring(14);
                        return new Item(artist, release, href, genre);
                    });
                }).stream().flatMap(itemStream -> itemStream);
    }

    public List<NoiseGenre> scrapeGenres() throws IOException {
        Document doc = Jsoup.connect(GENRE_PLAYLIST).maxBodySize(0).get();
        return VirtualParallel.runIO(doc.select("tr[valign=top]").stream().toList(), z -> {
            Element linkElement = z.child(1);
            Element nameElement = z.child(2);
            String href = linkElement.child(0).attr("href").substring(length);
            String genreName = nameElement.child(0).html();
            return new NoiseGenre(genreName, href);
        }).stream().toList();
    }

    public List<ReleaseWithGenres> scrape(List<String> genres, LocalDate week) throws UncheckedIOException {
        String collect = genres.stream().map(z -> URLEncoder.encode(z, StandardCharsets.UTF_8)).collect(Collectors.joining("%2C"));
        List<String> pages = TextSplitter.split(collect, URL_LENGTH, "%2C");

        return pages.stream().map(page -> {
                    try {
                        String formatted = GENRE_URL.formatted(page, week.format(DateTimeFormatter.BASIC_ISO_DATE));
                        return Jsoup.connect(formatted).maxBodySize(0).get();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }).flatMap(EveryNoiseScrapper::albumToItem)
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.groupingBy(z -> new GenreLess(z.artist(), z.release(), z.href()),
                                        Collectors.mapping(Item::genre, Collectors.toSet())),
                                setMap -> setMap.entrySet().stream().map(z -> new ReleaseWithGenres(z.getKey().artist(), z.getKey().release(), z.getKey().href(), z.getValue()))
                                        .toList()));
    }


    record Item(String artist, String release, String href, String genre) {
    }

    record GenreLess(String artist, String release, String href) {
    }


}
