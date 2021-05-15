package core.music.sources.youtube.webscrobbler.processers;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import core.music.sources.youtube.webscrobbler.YoutubeFilters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleProcesser implements YtSingleProcessser {
    private static final Pattern regex1 = Pattern.compile("^((\\[[^]]+])|(【[^】]+】))\\s*-*\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern regex2 = Pattern.compile("^\\s*([a-zA-Z]{1,2}|[0-9]{1,2})[1-9]?\\.\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern regex3 = Pattern.compile("-\\s*([「【『])", Pattern.CASE_INSENSITIVE);
    private static final Pattern regex4 = Pattern.compile("[(【].*?((MV)|(PV)).*?[】)]", Pattern.CASE_INSENSITIVE);
    private static final Pattern regex5 = Pattern.compile("[(【]((オリジナル)|(東方)).*?[】)]", Pattern.CASE_INSENSITIVE);
    private static final Pattern regex6 = Pattern.compile("(MV|PV)([「【『』】」]|$)", Pattern.CASE_INSENSITIVE);
    private static final TitleMatcher[] matchers = new TitleMatcher[]{
            new TitleMatcher(Pattern.compile("(.+?)([\\s:—-])+\\s*\"(.+?)\""), 1, 3),
            new TitleMatcher(Pattern.compile("(.+?)[『｢「](.+?)[」｣』]"), 1, 2),
            new TitleMatcher(Pattern.compile("(\\w[\\s\\w]*?)\\s+\\([^)]*\\s*by\\s*([^)]+)+\\)"), 2, 1)
    };
    private static final String[] delims = new String[]{"--",
            "--",
            " ~ ",
            " \u002d ",
            " \u2013 ",
            " \u2014 ",
            " // ",
            "\u002d",
            "\u2013",
            "\u2014",
            ":",
            "|",
            "///",
            "/",
            "~"};

    private static String processChannelName(JsonBrowser details) {
        return details.get("videoDetails").get("author").safeText();
    }

    static Processed splitArtistTrack(String str) {
        String first;
        String second;

        if (str != null) {
            Integer index = null;
            Integer length = null;
            for (String s : delims) {
                int i = str.indexOf(s);
                if (i > -1) {
                    index = i;
                    length = s.length();
                    break;
                }
            }

            if (index != null) {
                first = str.substring(0, index);
                second = str.substring(index + length);
                return new Processed(first, null, second);
            }
        }
        return null;
    }

    public Processed processTitle(String videoTitle, String author) {
        String artist = null;
        String song = null;
        if (videoTitle == null) {
            return null;
        }
        var title = regex1.matcher(videoTitle).replaceAll("");
        title = regex2.matcher(title).replaceAll("");
        title = regex3.matcher(title).replaceAll("$1");
        title = regex4.matcher(title).replaceAll("");
        title = regex5.matcher(title).replaceAll("");
        title = regex6.matcher(title).replaceAll("$2");

        for (TitleMatcher titleMatcher : matchers) {
            Matcher matcher = titleMatcher.pattern.matcher(title);
            if (matcher.find()) {
                artist = matcher.group(titleMatcher.groupArtist);
                song = matcher.group(titleMatcher.groupTrack);
                break;
            }
        }
        if (artist == null || song == null) {
            Processed processed = splitArtistTrack(title);
            if (processed != null) {
                artist = processed.artist();
                song = processed.song();
            }
        }
        if (artist == null || song == null) {
            Pattern compile = Pattern.compile("(.+?)【(.+?)】");
            Matcher matcher = compile.matcher(title);
            if (matcher.matches()) {
                artist = matcher.group(1);
                song = matcher.group(2);
            }
        }
        if (artist == null || song == null) {
            song = title;
            artist = author;
        }
        song = YoutubeFilters.doFilters(song);
        artist = YoutubeFilters.doFilters(artist);

        return new Processed(artist, null, song);
    }

    @Override
    public Processed processSingle(JsonBrowser details, JsonBrowser main) {
        String videoTitle = details.get("videoDetails").get("title").safeText();
        String s = processChannelName(details);
        return processTitle(videoTitle, s);
    }

    private record TitleMatcher(Pattern pattern, int groupArtist, int groupTrack) {
    }
}
