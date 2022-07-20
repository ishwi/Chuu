package core.parsers.params;

import core.parsers.ChartParserAux;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import org.apache.commons.text.WordUtils;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum UserConfigType {

    CHART_MODE("chart"),
    NOTIFY_IMAGE("image-notify"),
    PRIVATE_UPDATE("private-update"),
    WHOKNOWS_DISPLAY_MODE("whoknows-display"),
    REMAINING_MODE("rest"),
    CHART_SIZE("size"),
    PRIVACY_MODE("privacy"),
    NOTIFY_RATING("rating-notify"),
    PRIVATE_LASTFM("private-lastfm"),
    SHOW_BOTTED("show-botted"),
    NP("np"),
    SCROBBLING("scrobbling"),
    COLOR("color"),
    OWN_TAGS("own-tags"),
    ARTIST_THRESHOLD("artist-threshold"),
    CHART_OPTIONS("chart-options"),
    TIMEZONE("timezone"),

    WK_MODE("wk-mode");

    static final Pattern bool = Pattern.compile("(True|False)", Pattern.CASE_INSENSITIVE);
    static final Pattern chartMode = Pattern.compile("(Image|Image-info|Image-Aside|Image-aside-info|Pie|List|Clear)", Pattern.CASE_INSENSITIVE);
    static final Pattern whoknowsMode = Pattern.compile("(Image|Pie|List|Clear)", Pattern.CASE_INSENSITIVE);
    static final Pattern chartOptions = Pattern.compile("((--|—|~~)? ?(no([\\-_ ])?titles|plays|Clear)[ |&,]*)+", Pattern.CASE_INSENSITIVE);
    static final Pattern privacyMode = Pattern.compile("(Normal|Tag|Last-name|Discord-Name)", Pattern.CASE_INSENSITIVE);
    static final Pattern npMode = Pattern.compile("((" +
                                                  EnumSet.allOf(NPMode.class).stream().filter(x -> !x.equals(NPMode.UNKNOWN)).map(NPMode::toString).collect(Collectors.joining("|")) +
                                                  "|clear|list|add|remove|help|)[ |&,]*)+", Pattern.CASE_INSENSITIVE);
    static final Pattern wkImageMode = Pattern.compile("((" +
                                                       EnumSet.allOf(WKMode.class).stream().filter(x -> !x.equals(WKMode.UNKNOWN)).map(WKMode::toString).collect(Collectors.joining("|")) +
                                                       "|clear|list|add|remove|help|)[ |&,]*)+", Pattern.CASE_INSENSITIVE);
    static final Predicate<String> stringPredicate = (x) ->
    {
        try {
            ZoneOffset.of(x);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    };
    private static final Map<String, UserConfigType> ENUM_MAP;

    static {
        ENUM_MAP = Stream.of(UserConfigType.values())
                .collect(Collectors.toMap(UserConfigType::getCommandName, Function.identity()));
    }

    private final String commandName;

    UserConfigType(String command) {
        commandName = command;
    }

    public static UserConfigType get(String name) {
        return ENUM_MAP.get(name);
    }

    public static String list(ChuuService dao, long discordId) {
        return ENUM_MAP.entrySet().stream().map(
                        x -> {
                            String key = x.getKey();
                            LastFMData lastFMData;
                            try {
                                lastFMData = dao.findLastFMData(discordId);
                            } catch (InstanceNotFoundException e) {
                                lastFMData = null;
                            }

                            switch (x.getValue()) {
                                case PRIVATE_UPDATE: {
                                    boolean privateUpdate = lastFMData != null && lastFMData.isPrivateUpdate();
                                    return String.format("**%s** ➜ %s", key, privateUpdate);
                                }
                                case NOTIFY_IMAGE: {
                                    boolean privateUpdate;
                                    privateUpdate = lastFMData != null && lastFMData.isImageNotify();
                                    return String.format("**%s** ➜ %s", key, privateUpdate);
                                }
                                case CHART_MODE:
                                    String chartMode;
                                    if (lastFMData == null || lastFMData.getChartMode() == null) {
                                        chartMode = "NOT SET";
                                    } else {
                                        chartMode = lastFMData.getChartMode().toString();
                                    }
                                    return String.format("**%s** ➜ %s", key, chartMode);
                                case WHOKNOWS_DISPLAY_MODE:
                                    String whoknowsmode;
                                    if (lastFMData == null || lastFMData.getWhoKnowsMode() == null) {
                                        whoknowsmode = "NOT SET";
                                    } else {
                                        whoknowsmode = lastFMData.getWhoKnowsMode().toString();
                                    }
                                    return String.format("**%s** ➜ %s", key, whoknowsmode);
                                case REMAINING_MODE: {
                                    String remaining;
                                    if (lastFMData == null || lastFMData.getRemainingImagesMode() == null) {
                                        remaining = "NOT SET";
                                    } else {
                                        remaining = lastFMData.getRemainingImagesMode().toString();
                                    }
                                    return String.format("**%s** ➜ %s", key, remaining);
                                }
                                case CHART_SIZE: {
                                    String remaining;
                                    if (lastFMData == null) {
                                        remaining = "NOT SET";
                                    } else {
                                        remaining = String.format("%dx%d", lastFMData.getDefaultX(), lastFMData.getDefaultY());
                                    }
                                    return String.format("**%s** ➜ %s", key, remaining);
                                }
                                case PRIVACY_MODE: {
                                    String remaining;
                                    if (lastFMData == null) {
                                        remaining = "NOT SET";
                                    } else {
                                        remaining = String.format("%s", lastFMData.getPrivacyMode().toString());
                                    }
                                    return String.format("**%s** ➜ %s", key, remaining);
                                }
                                case NOTIFY_RATING: {
                                    boolean privateUpdate;
                                    privateUpdate = lastFMData != null && lastFMData.isRatingNotify();
                                    return String.format("**%s** ➜ %s", key, privateUpdate);
                                }
                                case PRIVATE_LASTFM:
                                    boolean privateLastfmId = lastFMData != null && lastFMData.isPrivateLastfmId();
                                    return String.format("**%s** ➜ %s", key, privateLastfmId);
                                case SHOW_BOTTED:
                                    boolean showBotted = lastFMData != null && lastFMData.isShowBotted();
                                    return String.format("**%s** ➜ %s", key, showBotted);
                                case NP:
                                    EnumSet<NPMode> modes = dao.getNPModes(discordId);
                                    String strModes = NPMode.getListedName(modes);
                                    return String.format("**%s** ➜ %s", key, strModes);
                                case SCROBBLING:
                                    boolean scroobling = lastFMData != null && lastFMData.isScrobbling();
                                    return String.format("**%s** ➜ %s", key, scroobling);
                                case COLOR:
                                    return String.format("**%s** ➜ %s", key, lastFMData == null || lastFMData.getEmbedColor() == null ? EmbedColor.defaultColor().toDisplayString() : lastFMData.getEmbedColor().toDisplayString());
                                case OWN_TAGS:
                                    boolean ownTags = lastFMData != null && lastFMData.useOwnTags();
                                    return String.format("**%s** ➜ %s", key, ownTags);
                                case ARTIST_THRESHOLD: {
                                    String remaining;
                                    if (lastFMData == null) {
                                        remaining = "NOT SET";
                                    } else {
                                        remaining = String.valueOf(lastFMData.getArtistThreshold());
                                    }
                                    return String.format("**%s** ➜ %s", key, remaining);
                                }
                                case CHART_OPTIONS:
                                    String remaining;
                                    if (lastFMData == null) {
                                        remaining = "NOT SET";
                                    } else {
                                        EnumSet<ChartOptions> chartOptions = lastFMData.getChartOptions();
                                        remaining = ChartOptions.getListedName(chartOptions);
                                    }
                                    return String.format("**%s** ➜ %s", key, remaining);
                                case TIMEZONE:
                                    return null;
                            }
                            return null;
                        })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

    }

    public String getCommandName() {
        return commandName;
    }

    public Predicate<String> getParser() {
        return switch (this) {
            case CHART_MODE -> chartMode.asMatchPredicate();
            case REMAINING_MODE, WHOKNOWS_DISPLAY_MODE -> whoknowsMode.asMatchPredicate();
            case PRIVACY_MODE -> privacyMode.asMatchPredicate();
            case PRIVATE_UPDATE, NOTIFY_IMAGE, NOTIFY_RATING, PRIVATE_LASTFM, SHOW_BOTTED, SCROBBLING, OWN_TAGS ->
                    bool.asMatchPredicate();
            case CHART_SIZE -> ChartParserAux.chartSizePattern.asMatchPredicate();
            case NP -> npMode.asMatchPredicate();
            case COLOR -> GuildConfigType.colorMode.asMatchPredicate();
            case ARTIST_THRESHOLD -> GuildConfigType.number.asMatchPredicate();
            case CHART_OPTIONS -> chartOptions.asMatchPredicate();
            case TIMEZONE -> stringPredicate;
            case WK_MODE -> wkImageMode.asMatchPredicate();
        };
    }

    public String getExplanation() {
        return switch (this) {
            case PRIVATE_UPDATE ->
                    "If you want others users to be able to update your account with a ping and the update command (true for making it private, false for it to be public)";
            case NOTIFY_IMAGE ->
                    "Whether you will get notified or not when a submitted image gets accepted (true = notify, false = don't)";
            case CHART_MODE -> {
                String line = EnumSet.allOf(ChartMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                line += "\n\t\t\t**Clear**: Sets the default mode";
                yield "Set the mode for all charts. " +
                      "Keep in mind that if a server has a set value that will be prioritized.\n" +
                      "\t\tThe possible values for the chart mode are the following:" + line;
            }
            case WHOKNOWS_DISPLAY_MODE -> {
                String line = EnumSet.allOf(WhoKnowsDisplayMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                line += "\n\t\t\t**Clear**: Sets the default mode";
                yield "Set the mode for all charts. " +
                      "Keep in mind that if a server has a set value that will be prioritized.\n" +
                      "\t\tThe possible values for the who knows mode are the following:" + line;
            }
            case REMAINING_MODE -> {
                String line = EnumSet.allOf(RemainingImagesMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                line += "\n\t\t\t**Clear**:| Sets the default mode";
                yield "Set the mode for the rest of the commands. " +
                      "Keep in mind that if a server has a set value that will be prioritized.\n" +
                      "\t\tThe possible values for the rest of the commands are the following:" + line;
            }
            case CHART_SIZE ->
                    "Change the default chart size for chart command when you dont specify directly the size";
            case PRIVACY_MODE ->
                    "Sets how will you appear in the global leaderboard, changing this means users from other servers might be able to contact you directly";
            case NOTIFY_RATING ->
                    "Whether you will get notified or not when a url you have submitted to the random command gets rated by someone else (true = notify, false = don't)";
            case PRIVATE_LASTFM ->
                    "Setting this to true will mean that your last.fm name will stay private and will not be shared with anyone. (This is different from privacy settings since it affects commands within a server and not cross server)";
            case SHOW_BOTTED ->
                    "Setting this to false will mean that you wont have to include --nobotted in the global commands to exclude accounts flagged as bots)";
            case NP ->
                    "Setting this will alter the appearance of your np commands. You can select as many as you want from the following list and mix them up:\n" + NPMode.getListedName(EnumSet.allOf(NPMode.class));
            case SCROBBLING ->
                    "Setting this to false will mean that whatever you play with the bot on a voice channel won't scrooble";
            case COLOR -> {
                String line = EnumSet.allOf(EmbedColor.EmbedColorType.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                yield "Set the color for your embeds.\n" +
                      "\t\tThe possible values for the embed colour are the following:" + line;
            }
            case OWN_TAGS ->
                    "Setting this to true will mean that for the np command your own tags will be prioritized. (Need also to authorize the bot with `login`)";
            case ARTIST_THRESHOLD ->
                    "Changes the minimun number of plays required for an album to show on the artist command";
            case CHART_OPTIONS ->
                    "Specify some chart options that will apply as default for all your charts. Right now only --plays and --notitles";
            case TIMEZONE -> "TIMEZONE ";
            case WK_MODE ->
                    "Sets the wkmode when you are using the image mode; You can select as many as you want from the following list and mix them up:" + WKMode.getListedName(EnumSet.allOf(WKMode.class));
        };
    }
}

