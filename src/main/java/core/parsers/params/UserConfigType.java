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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum UserConfigType {

    CHART_MODE("chart"), NOTIFY_IMAGE("image-notify"), PRIVATE_UPDATE("private-update"), WHOKNOWS_MODE("whoknows"),
    REMAINING_MODE("rest"),
    CHART_SIZE("size"),
    PRIVACY_MODE("privacy"),
    NOTIFY_RATING("rating-notify"),
    PRIVATE_LASTFM("private-lastfm"),
    SHOW_BOTTED("show-botted"),
    NP("np"),
    SCROBBLING("scrobbling"),
    COLOR("color"),
    TIMEZONE("timezone");

    static final Pattern bool = Pattern.compile("(True|False)", Pattern.CASE_INSENSITIVE);
    static final Pattern chartMode = Pattern.compile("(Image|Image-info|Image-Aside|Image-aside-info|Pie|List|Clear)", Pattern.CASE_INSENSITIVE);
    static final Pattern whoknowsMode = Pattern.compile("(Image|Pie|List|Clear)", Pattern.CASE_INSENSITIVE);
    static final Pattern privacyMode = Pattern.compile("(Normal|Tag|Last-name|Discord-Name)", Pattern.CASE_INSENSITIVE);
    static final Pattern npMode = Pattern.compile("((" +
            EnumSet.allOf(NPMode.class).stream().filter(x -> !x.equals(NPMode.UNKNOWN)).map(NPMode::toString).collect(Collectors.joining("|")) +
            ")[ |&,]*)+", Pattern.CASE_INSENSITIVE);
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
                            return String.format("**%s** -> %s", key, privateUpdate);
                        }
                        case NOTIFY_IMAGE: {
                            boolean privateUpdate;
                            privateUpdate = lastFMData != null && lastFMData.isImageNotify();
                            return String.format("**%s** -> %s", key, privateUpdate);
                        }
                        case CHART_MODE:
                            String chartMode;
                            if (lastFMData == null || lastFMData.getChartMode() == null) {
                                chartMode = "NOT SET";
                            } else {
                                chartMode = lastFMData.getChartMode().toString();
                            }
                            return String.format("**%s** -> %s", key, chartMode);
                        case WHOKNOWS_MODE:
                            String whoknowsmode;
                            if (lastFMData == null || lastFMData.getWhoKnowsMode() == null) {
                                whoknowsmode = "NOT SET";
                            } else {
                                whoknowsmode = lastFMData.getWhoKnowsMode().toString();
                            }
                            return String.format("**%s** -> %s", key, whoknowsmode);
                        case REMAINING_MODE: {
                            String remaining;
                            if (lastFMData == null || lastFMData.getRemainingImagesMode() == null) {
                                remaining = "NOT SET";
                            } else {
                                remaining = lastFMData.getRemainingImagesMode().toString();
                            }
                            return String.format("**%s** -> %s", key, remaining);
                        }
                        case CHART_SIZE: {
                            String remaining;
                            if (lastFMData == null) {
                                remaining = "NOT SET";
                            } else {
                                remaining = String.format("%dx%d", lastFMData.getDefaultX(), lastFMData.getDefaultY());
                            }
                            return String.format("**%s** -> %s", key, remaining);
                        }
                        case PRIVACY_MODE: {
                            String remaining;
                            if (lastFMData == null) {
                                remaining = "NOT SET";
                            } else {
                                remaining = String.format("%s", lastFMData.getPrivacyMode().toString());
                            }
                            return String.format("**%s** -> %s", key, remaining);
                        }
                        case NOTIFY_RATING: {
                            boolean privateUpdate;
                            privateUpdate = lastFMData != null && lastFMData.isRatingNotify();
                            return String.format("**%s** -> %s", key, privateUpdate);
                        }
                        case PRIVATE_LASTFM:
                            boolean privateLastfmId = lastFMData != null && lastFMData.isPrivateLastfmId();
                            return String.format("**%s** -> %s", key, privateLastfmId);
                        case SHOW_BOTTED:
                            boolean showBotted = lastFMData != null && lastFMData.isShowBotted();
                            return String.format("**%s** -> %s", key, showBotted);
                        case NP:
                            EnumSet<NPMode> modes = dao.getNPModes(discordId);
                            String strModes = NPMode.getListedName(modes);
                            return String.format("**%s** -> %s", key, strModes);
                        case SCROBBLING:
                            boolean scroobling = lastFMData != null && lastFMData.isScrobbling();
                            return String.format("**%s** -> %s", key, scroobling);
                        case COLOR:
                            return String.format("**%s** -> %s", key, lastFMData == null || lastFMData.getEmbedColor() == null ? EmbedColor.defaultColor() : lastFMData.getEmbedColor().toDisplayString());
                        case TIMEZONE:
                            return "";
                    }
                    return null;
                }).

                collect(Collectors.joining("\n"));

    }

    public String getCommandName() {
        return commandName;
    }

    public Predicate<String> getParser() {
        return switch (this) {
            case CHART_MODE -> chartMode.asMatchPredicate();
            case REMAINING_MODE, WHOKNOWS_MODE -> whoknowsMode.asMatchPredicate();
            case PRIVACY_MODE -> privacyMode.asMatchPredicate();
            case PRIVATE_UPDATE, NOTIFY_IMAGE, NOTIFY_RATING, PRIVATE_LASTFM, SHOW_BOTTED, SCROBBLING -> bool.asMatchPredicate();
            case CHART_SIZE -> ChartParserAux.chartSizePattern.asMatchPredicate();
            case NP -> npMode.asMatchPredicate();
            case COLOR -> GuildConfigType.colorMode.asMatchPredicate();
            case TIMEZONE -> stringPredicate;
        };
    }

    public String getExplanation() {
        switch (this) {
            case PRIVATE_UPDATE:
                return "If you want others users to be able to update your account with a ping and the update command (true for making it private, false for it to be public)";
            case NOTIFY_IMAGE:
                return "Whether you will get notified or not when a submitted image gets accepted (true = notify, false = don't)";
            case CHART_MODE:
                String collect = EnumSet.allOf(ChartMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                collect += "\n\t\t\t**Clear**: Sets the default mode";
                return "Set the mode for all charts. " +
                        "Keep in mind that if a server has a set value that will be prioritized.\n" +
                        "\t\tThe possible values for the chart mode are the following:" + collect;
            case WHOKNOWS_MODE:
                collect = EnumSet.allOf(WhoKnowsMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                collect += "\n\t\t\t**Clear**: Sets the default mode";
                return "Set the mode for all charts. " +
                        "Keep in mind that if a server has a set value that will be prioritized.\n" +
                        "\t\tThe possible values for the who knows mode are the following:" + collect;
            case REMAINING_MODE:
                collect = EnumSet.allOf(RemainingImagesMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                collect += "\n\t\t\t**Clear**:| Sets the default mode";
                return "Set the mode for the rest of the commands. " +
                        "Keep in mind that if a server has a set value that will be prioritized.\n" +
                        "\t\tThe possible values for the rest of the commands are the following:" + collect;
            case CHART_SIZE:
                return "Change the default chart size for chart command when you dont specify directly the size";
            case PRIVACY_MODE:
                return "Sets how will you appear in the global leaderboard, changing this means users from other servers might be able to contact you directly";
            case NOTIFY_RATING:
                return "Whether you will get notified or not when a url you have submitted to the random command gets rated by someone else (true = notify, false = don't)";
            case PRIVATE_LASTFM:
                return "Setting this to true will mean that your last.fm name will stay private and will not be shared with anyone. (This is different from privacy settings since it affects commands within a server and not cross server)";
            case SHOW_BOTTED:
                return "Setting this to false will mean that you wont have to include --nobotted in the global commands to exclude accounts flagged as bots)";
            case NP:
                return "Setting this will alter the appearance of your np commands. You can select as many as you want from the following list and mix them up:\n" + NPMode.getListedName(EnumSet.allOf(NPMode.class));
            case SCROBBLING:
                return "Setting this to false will mean that whatever you play with the bot on a voice channel won't scrooble";
            case COLOR:
                collect = EnumSet.allOf(EmbedColor.EmbedColorType.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                return "Set the color for your embeds.\n" +
                        "\t\tThe possible values for the embed colour are the following:" + collect;
            case TIMEZONE:
                return "TIMEZONE ";
            default:
                throw new IllegalStateException();
        }
    }
}

