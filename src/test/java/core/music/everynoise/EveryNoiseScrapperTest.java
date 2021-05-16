package core.music.everynoise;

import core.apis.lyrics.TextSplitter;
import dao.SimpleDataSource;
import dao.everynoise.EveryNoiseServiceImpl;
import dao.everynoise.ReleaseWithGenres;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class EveryNoiseScrapperTest {
    @Test
    public void name() throws IOException {
        Document doc = Jsoup.connect("http://everynoise.com/new_releases_by_genre.cgi?region=US&albumsonly=&style=cards&date=&genre=anygenre&artistsfrom=").get();
        String collect = doc.select(".genre").parallelStream().map(z -> URLEncoder.encode(z.child(0).html(), StandardCharsets.UTF_8)).collect(Collectors.joining(","));
        int size = 8083;
        List<String> pages = TextSplitter.split(collect, size, ",");
        System.out.println(pages.size());
        pages.forEach(t -> System.out.println(t.length()));


        pages.forEach(System.out::println);
    }

    @Test
    public void testItem() {
        String item = """
                \t<span class=play trackid=4g1DpKaLPoPjVIMJTcNo05 preview_url="https://p.scdn.co/mp3-preview/cb7ae213d1aa541ad377975bf0651801f0593d07" nolink=true onclick="playmeta(this)"><img src="http://i.scdn.co/image/ab67616d00001e0291b3632ddab182e9f4cb9945" width=150 height=150 class="albumart"></span><br><a
                \t\t\t\t\t\t\t\t\turi="?genre=%21spotify%3Aartist%3A6tlPbmYME3fjRkSBb3pjoB&region=GB&albumsonly=true&hidedupes=on"
                \t\t\t\t\t\t\t\t\ttitle="artist 112 in genre
                GB rank 178,974"><b>Eddie Boyd</b></a><br><a onclick="this.setAttribute('visited', true);"
                \t\t\t\t\t\t\t\t\turi="spotify:album:0PfYDFGbbrD8DyPys7g9U6"><i>Rare Oldies Jazz: Eddy Boyd</i></a>
                \t\t\t\t\t\t\t\t<span class=trackcount title="tracks">19</span><span class=unnew title="all tracks previously released as of 2013-01-01">⟲</span>""";
        Document doc = Jsoup.parse(item);
        Element b = doc.selectFirst("b");
        Element i = doc.selectFirst("i");
        String artist = b.html();
        String release = i.html();
        String href = i.parent().attr("uri");
        assertEquals(artist, "Eddie Boyd");
        assertEquals(release, "Rare Oldies Jazz: Eddy Boyd");
        assertEquals(href, "spotify:album:0PfYDFGbbrD8DyPys7g9U6");

    }

    @Test
    public void b() {
//        String s = null;
//        try (InputStream in = ImageUtils.class.getResourceAsStream("/blues.html")) {
//            assert in != null;
//            s = IOUtils.toString(in, StandardCharsets.UTF_8);
//        }
//        assert s != null;
//        Document doc = Jsoup.parse(s);
//        List<? extends EveryNoiseScrapper.Item> items = EveryNoiseScrapper.albumToItem(doc).toList();
//        items.forEach(System.out::println);
//        System.out.println(items.size());

    }

    @Test
    public void out() {
        EveryNoiseServiceImpl everyNoiseService = new EveryNoiseServiceImpl(new SimpleDataSource(true));
        ReleaseWithGenres e1 = new ReleaseWithGenres("artist1", "release2", "uri", Set.of("tanci", "glass"));
        ReleaseWithGenres e2 = new ReleaseWithGenres("artist2", "release3", "uri", Set.of("tanci", "classical bassoon"));
        LocalDate week = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));

        everyNoiseService.insertReleases(List.of(e1, e2), week);
    }
}
