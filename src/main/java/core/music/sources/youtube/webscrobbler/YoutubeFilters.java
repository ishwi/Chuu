package core.music.sources.youtube.webscrobbler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class YoutubeFilters {
    private static final LinkedHashMap<Pattern, String> ytFilter;
    private static final LinkedHashMap<Pattern, String> trimFilter;
    private static final LinkedHashMap<Pattern, String> extraFilters;

    static {
        ytFilter = generateYoutubeFilters();

        LinkedHashMap<Pattern, String> filters = new LinkedHashMap<>();
        // Leftovers after e.g. (official video)
        filters.put(Pattern.compile("\\(+\\s*\\)+"), "");
        // trim starting white chars and dash
        filters.put(Pattern.compile("^[/,:;~\\s\"-]"), "");
        filters.put(Pattern.compile("^[/,:;~\\s\"-]+$"), "");
        filters.put(Pattern.compile("\\s+"), " ");
        trimFilter = filters;


        LinkedHashMap<Pattern, String> extra = new LinkedHashMap<>();
        // Leftovers after e.g. (official video)
        extra.put(Pattern.compile("[\\u200e\\u200f]"), "");
        extra.put(Pattern.compile("^\\d{1,2}[.)]\\s?"), "");
        extra.put(Pattern.compile("^\\(\\d{1,2}\\)\\."), "");
        extraFilters = extra;

    }

    private static LinkedHashMap<Pattern, String> generateYoutubeFilters() {
        LinkedHashMap<Pattern, String> filters = new LinkedHashMap<>();
        filters.put(Pattern.compile("^\\s+|\\s+$"), "");
        filters.put(Pattern.compile("\\*+\\s?\\S+\\s?\\*+$"), "");
        filters.put(Pattern.compile("\\[[^]]+]"), "");
        // (whatever version)
        filters.put(Pattern.compile("\\([^)]*version\\)$", Pattern.CASE_INSENSITIVE), "");
        // [whatever]
        filters.put(Pattern.compile("\\[[^]]+]"), "");
        // video extensions
        filters.put(Pattern.compile("\\[[^]]+]"), "");
        // (LYRICs VIDEO)
        filters.put(Pattern.compile("\\(.*lyrics?\\s*(video)?\\)", Pattern.CASE_INSENSITIVE), "");
        // (Official Track Stream)
        filters.put(Pattern.compile("\\((of+icial\\s*)?(track\\s*)?stream\\)", Pattern.CASE_INSENSITIVE), "");
        // (official)? (music)? video
        filters.put(Pattern.compile("\\((of+icial\\s*)?(music|art)?\\s*video\\)", Pattern.CASE_INSENSITIVE), "");
        // (official)? (music)? audio
        filters.put(Pattern.compile("\\((of+icial\\s*)?(music|art)?\\s*audio\\)", Pattern.CASE_INSENSITIVE), "");
        // (ALBUM TRACK)
        filters.put(Pattern.compile("(ALBUM TRACK\\s*)?(album track\\s*)", Pattern.CASE_INSENSITIVE), "");
        // (Cover Art)
        filters.put(Pattern.compile("(COVER ART\\s*)?(Cover Art\\s*)", Pattern.CASE_INSENSITIVE), "");
        // (official)
        filters.put(Pattern.compile("\\(\\s*of+icial\\s*\\)", Pattern.CASE_INSENSITIVE), "");


        // (1999)
        filters.put(Pattern.compile("\\(\\s*[0-9]{4}\\s*\\)", Pattern.CASE_INSENSITIVE), "");
        // (HD) / (HQ)
        filters.put(Pattern.compile("\\(\\s*(HD|HQ)\\s*\\)$"), "");
        // HD / HQ
        filters.put(Pattern.compile("(HD|HQ)\\s?$"), "");
        // video clip officiel or video clip official
        filters.put(Pattern.compile("(vid[\u00E9e]o)?\\s?clip\\sof+ici[ae]l", Pattern.CASE_INSENSITIVE), "");
        // offizielles
        filters.put(Pattern.compile("of+iziel+es\\s*video", Pattern.CASE_INSENSITIVE), "");
        // video clip
        filters.put(Pattern.compile("vid[\u00E9e]o\\s?clip", Pattern.CASE_INSENSITIVE), "");
        // clip
        filters.put(Pattern.compile("\\sclip", Pattern.CASE_INSENSITIVE), "");
        // Full Album
        filters.put(Pattern.compile("\\(?\\s*full\\s*album\\s*\\)?", Pattern.CASE_INSENSITIVE), "");
        // (live)
        filters.put(Pattern.compile("\\(live.*?\\)$", Pattern.CASE_INSENSITIVE), "");
        // | something
        filters.put(Pattern.compile("\\|.*$", Pattern.CASE_INSENSITIVE), "");

        // (*01/01/1999*)
        filters.put(Pattern.compile("\\(.*[0-9]{1,2}/[0-9]{1,2}/[0-9]{2,4}.*\\)", Pattern.CASE_INSENSITIVE), "");
        // Sub Español
        filters.put(Pattern.compile("sub\\s*español", Pattern.CASE_INSENSITIVE), "");
        // (Letra/Lyrics)
        filters.put(Pattern.compile("\\s?(with|con)?\\s\\(Letra/Lyrics\\)", Pattern.CASE_INSENSITIVE), "");
        filters.put(Pattern.compile("\\s?(with|con)?\\s(Letra|Lyrics)", Pattern.CASE_INSENSITIVE), "");
        // (Letra)
        filters.put(Pattern.compile("\\s\\(Letra\\)", Pattern.CASE_INSENSITIVE), "");
        // (En vivo)
        filters.put(Pattern.compile("\\s\\(En\\svivo\\)", Pattern.CASE_INSENSITIVE), "");
        // Sub Español
        filters.put(Pattern.compile("sub\\s*español", Pattern.CASE_INSENSITIVE), "");
        filters.put(Pattern.compile("^(|.*\\s)\"(.{5,})\"(\\s . *|)$"), "$2");
        filters.put(Pattern.compile("^(|.*\\s)'(.{5,})'(\\s.*|)$"), "$2");
        filters.put(Pattern.compile("\\u0332"), "");
        return filters;
    }

    public static String doFilters(String toProcess) {
        if (toProcess == null) {
            return null;
        }
        for (Map.Entry<Pattern, String> patternStringEntry : trimFilter.entrySet()) {
            Pattern key = patternStringEntry.getKey();
            String value = patternStringEntry.getValue();
            toProcess = key.matcher(toProcess).replaceAll(value);
        }
        for (Map.Entry<Pattern, String> patternStringEntry : ytFilter.entrySet()) {
            Pattern key = patternStringEntry.getKey();
            String value = patternStringEntry.getValue();
            toProcess = key.matcher(toProcess).replaceAll(value);
        }
        for (Map.Entry<Pattern, String> patternStringEntry : extraFilters.entrySet()) {
            Pattern key = patternStringEntry.getKey();
            String value = patternStringEntry.getValue();
            toProcess = key.matcher(toProcess).replaceAll(value);
        }
        return toProcess;
    }
}
