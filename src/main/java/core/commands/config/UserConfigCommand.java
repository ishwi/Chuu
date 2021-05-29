package core.commands.config;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.parsers.ChartParserAux;
import core.parsers.Parser;
import core.parsers.UserConfigParser;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.params.UserConfigParameters;
import core.parsers.params.UserConfigType;
import core.services.ColorService;
import dao.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import org.apache.commons.text.WordUtils;

import javax.validation.constraints.NotNull;
import java.awt.*;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class UserConfigCommand extends ConcurrentCommand<UserConfigParameters> {
    public UserConfigCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<UserConfigParameters> initParser() {
        return new UserConfigParser(db);
    }

    @Override
    public String getDescription() {
        return "Configuration per user";
    }

    @Override
    public List<String> getAliases() {
        return List.of("configuration", "config");
    }

    @Override
    public String getName() {
        return "User Configuration";
    }

    @Override
    protected void onCommand(Context e, @NotNull UserConfigParameters params) throws LastFmException, InstanceNotFoundException {

        UserConfigType config = params.getConfig();
        String value = params.getValue().trim();
        boolean cleansing = value.equalsIgnoreCase("clear");
        switch (config) {
            case PRIVATE_UPDATE:
                boolean b = Boolean.parseBoolean(value);
                db.setPrivateUpdate(e.getAuthor().getIdLong(), b);
                if (b) {
                    sendMessageQueue(e, "Successfully made private the update for user " + getUserString(e, e.getAuthor().getIdLong()));
                } else {
                    sendMessageQueue(e, "Successfully made non private the update for user " + getUserString(e, e.getAuthor().getIdLong()));
                }
                break;
            case COLOR:
                EmbedColor embedColor;
                if (cleansing) {
                    embedColor = null;
                } else {
                    embedColor = EmbedColor.fromString(value);
                    if (embedColor == null || embedColor.type() == EmbedColor.EmbedColorType.COLOURS && embedColor.colorList().isEmpty()) {
                        sendMessageQueue(e, "Couldn't read any colour :(\nTry with different values.");
                        return;
                    }
                    if (!embedColor.isValid()) {
                        parser.sendError("Too many colours were introduced. Pls reduce your input a bit", e);
                        return;
                    }
                }
                db.setEmbedColor(e.getAuthor().getIdLong(), embedColor);
                ColorService.handleUserChange(e.getAuthor().getIdLong(), embedColor);
                String str = embedColor == null ? "Default" : embedColor.toDisplayString();
                sendMessageQueue(e, "User color mode set to: **" + WordUtils.capitalizeFully(str) + "**");
                break;
            case NOTIFY_IMAGE:
                b = Boolean.parseBoolean(value);
                db.setImageNotify(e.getAuthor().getIdLong(), b);
                if (b) {
                    sendMessageQueue(e, "Now you will get notified whenever an image you uploaded gets approved");
                } else {
                    sendMessageQueue(e, "Now you will not get notified whenever an image you uploaded gets approved");
                }
                break;
            case CHART_MODE:
                ChartMode chartMode;
                if (cleansing) {
                    chartMode = ChartMode.IMAGE;
                } else {
                    chartMode = ChartMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                db.setChartEmbed(e.getAuthor().getIdLong(), chartMode);
                if (cleansing) {
                    sendMessageQueue(e, "Now your charts are back to the default");
                } else {
                    sendMessageQueue(e, "Chart mode set to: **" + WordUtils.capitalizeFully(chartMode.toString()) + "**");
                }
                break;
            case WHOKNOWS_MODE:
                WhoKnowsMode whoKnowsMode;
                if (cleansing) {
                    whoKnowsMode = WhoKnowsMode.IMAGE;
                } else {
                    whoKnowsMode = WhoKnowsMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                db.setWhoknowsMode(e.getAuthor().getIdLong(), whoKnowsMode);
                if (cleansing) {
                    sendMessageQueue(e, "Now your who knows are back to the default");
                } else {
                    sendMessageQueue(e, "Who Knows mode set to: **" + WordUtils.capitalizeFully(whoKnowsMode.toString()) + "**");
                }
                break;
            case REMAINING_MODE:
                RemainingImagesMode remainingImagesMode;
                if (cleansing) {
                    remainingImagesMode = RemainingImagesMode.IMAGE;
                } else {
                    remainingImagesMode = RemainingImagesMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                db.setRemainingImagesMode(e.getAuthor().getIdLong(), remainingImagesMode);
                if (cleansing) {
                    sendMessageQueue(e, "The mode of the remaining image commands to the default");
                } else {
                    sendMessageQueue(e, "The mode of the remaining image commands was set to: **" + WordUtils.capitalizeFully(remainingImagesMode.toString()) + "**");
                }
                break;
            case CHART_SIZE:
                ChartParserAux chartParserAux = new ChartParserAux(new String[]{value});
                int x;
                int y;
                try {
                    Point chartSize = chartParserAux.getChartSize();
                    if (chartSize == null) {
                        sendMessageQueue(e, "Something went wrong evaluating your chart size");
                        return;
                    }
                    x = (int) chartSize.getX();
                    y = (int) chartSize.getY();
                } catch (InvalidChartValuesException invalidChartValuesException) {
                    sendMessageQueue(e, "Something went wrong evaluating your chart size");
                    return;
                }
                if (x * y > 7 * 7) {
                    sendMessageQueue(e, "The default value can't be greater than 7x7");
                    return;
                }
                db.setChartDefaults(x, y, e.getAuthor().getIdLong());
                sendMessageQueue(e, "Successfully changed default chart size for user " + getUserString(e, e.getAuthor().getIdLong()));
                break;
            case PRIVACY_MODE:
                PrivacyMode privacyMode;
                if (cleansing) {
                    privacyMode = PrivacyMode.NORMAL;
                } else {
                    privacyMode = PrivacyMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                db.setPrivacyMode(e.getAuthor().getIdLong(), privacyMode);
                if (cleansing) {
                    sendMessageQueue(e, "Your privacy setting has changed to the default");
                } else {
                    String name = db.findLastFMData(e.getAuthor().getIdLong()).getName();
                    String publicStr = PrivacyUtils.getPublicString(privacyMode, e.getAuthor().getIdLong(), name, new AtomicInteger(1), e, new HashSet<>()).discordName();

                    sendMessageQueue(e, "Your privacy setting was set to: **%s**%nNow you will appear as **%s** for users in other servers".formatted(WordUtils.capitalizeFully(privacyMode.toString()), publicStr));
                }
            case NOTIFY_RATING:
                b = Boolean.parseBoolean(value);
                db.setImageNotify(e.getAuthor().getIdLong(), b);
                if (b) {
                    sendMessageQueue(e, "Now you will get notified whenever an url you uploaded to the random command gets rated");
                } else {
                    sendMessageQueue(e, "Now you will not get notified whenever an url you uploaded to the random command gets rated");
                }
                break;
            case PRIVATE_LASTFM:
                b = Boolean.parseBoolean(value);
                String name = db.findLastFMData(e.getAuthor().getIdLong()).getName();
                db.setPrivateLastfm(e.getAuthor().getIdLong(), b);
                Chuu.changePrivacyLastfm(name, b);
                if (b) {

                    sendMessageQueue(e, "Successfully made private the lastfm profile for user " + getUserString(e, e.getAuthor().getIdLong()));
                } else {
                    sendMessageQueue(e, "Successfully made non private the the lastfm profile for user " + getUserString(e, e.getAuthor().getIdLong()));
                }
                break;
            case SHOW_BOTTED:
                b = Boolean.parseBoolean(value);
                db.setShowBotted(e.getAuthor().getIdLong(), b);
                if (b) {
                    sendMessageQueue(e, "Will show botted accounts on the global leaderboard");
                } else {
                    sendMessageQueue(e, "Won't show botted accounts on the global leaderboard");
                }
                break;
            case NP:

                if (value.matches("(clear|list|add|remove|help).*")) {
                    sendMessageQueue(e, "To use one of those settings please use the `" + e.getPrefix() + "npc` command instead.");
                    return;
                }
                EnumSet<NPMode> modes = NPModeSetterCommand.mapper.apply(value);
                if (modes.isEmpty()) {
                    sendMessageQueue(e, "Couldn't parse any mode. Please to get more info use `" + e.getPrefix() + "npc help`");
                    return;
                }

                db.changeNpMode(e.getAuthor().getIdLong(), modes);
                String strModes = NPMode.getListedName(modes);
                sendMessageQueue(e, String.format("Successfully changed to the following %s: %s", CommandUtil.singlePlural(modes.size(), "mode", "modes"), strModes));
                break;
            case SCROBBLING:
                b = Boolean.parseBoolean(value);
                db.changeScrobbling(e.getAuthor().getIdLong(), b);
                if (b) {
                    sendMessageQueue(e, "Will scrobble what you play on a voice channel");
                } else {
                    sendMessageQueue(e, "Won't scrobble what you play on a voice channel");
                }
                break;
            case OWN_TAGS:
                b = Boolean.parseBoolean(value);
                db.setOwnTags(e.getAuthor().getIdLong(), b);
                if (b) {
                    sendMessageQueue(e, "Will prioritize your own tags for artist in the np command");
                } else {
                    sendMessageQueue(e, "Wont prioritize your own tags for artist in the np command");
                }
                break;
            case ARTIST_THRESHOLD:
                int threshold = Integer.parseInt(value);
                db.setArtistThreshold(e.getAuthor().getIdLong(), threshold);
                if (threshold > 0) {
                    sendMessageQueue(e, "Will filter out albums with less than %d plays in the artist commands");
                } else {
                    sendMessageQueue(e, "Won't filter out albums in the artist commands");

                }

                break;
            case CHART_OPTIONS:
                EnumSet<ChartOptions> chartOpts;
                if (value.toLowerCase(Locale.ROOT).contains("clear")) {
                    chartOpts = ChartOptions.defaultMode();
                } else {
                    String[] split = value.replaceAll("--|~~|—", "").replaceAll("\\s+", " ").split("[|,& ]+");
                    chartOpts = EnumSet.noneOf(ChartOptions.class);
                    for (String mode : split) {
                        ChartOptions npMode = ChartOptions.valueOf(mode.replace("-", "_").toUpperCase());
                        chartOpts.add(npMode);
                    }
                }
                if (chartOpts.isEmpty() || chartOpts.contains(ChartOptions.NONE)) {
                    chartOpts = ChartOptions.defaultMode();
                }
                db.changeChartMode(e.getAuthor().getIdLong(), chartOpts);
                strModes = ChartOptions.getListedName(chartOpts);
                sendMessageQueue(e, String.format("Successfully changed to the following %s: %s", CommandUtil.singlePlural(chartOpts.size(), "chart options", "chart options"), strModes));
                break;
            case TIMEZONE:
                break;
        }
    }
}
