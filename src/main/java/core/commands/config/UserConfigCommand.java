package core.commands.config;

import core.Chuu;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartParserAux;
import core.parsers.Parser;
import core.parsers.UserConfigParser;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.params.UserConfigParameters;
import core.parsers.params.UserConfigType;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.text.WordUtils;

import javax.validation.constraints.NotNull;
import java.awt.*;
import java.util.EnumSet;
import java.util.List;

public class UserConfigCommand extends ConcurrentCommand<UserConfigParameters> {
    public UserConfigCommand(ChuuService dao) {
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
    protected void onCommand(MessageReceivedEvent e, @NotNull UserConfigParameters params) throws LastFmException, InstanceNotFoundException {
        UserConfigParameters parse = this.parser.parse(e);

        UserConfigType config = parse.getConfig();
        String value = parse.getValue();
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
                EmbedColor embedColor = EmbedColor.fromString(value);
                if (embedColor.type() == EmbedColor.EmbedColorType.COLOURS && embedColor.colorList().isEmpty()) {
                    sendMessageQueue(e, "Couldn't read any colour :(\nTry with different values.");
                    return;
                }
                if (!embedColor.isValid()) {
                    parser.sendError("Too many colours were introduced. Pls reduce your input a bit", e);
                    return;
                }
                db.setEmbedColor(e.getAuthor().getIdLong(), embedColor);
                ColorService.handleUserChange(e.getAuthor().getIdLong(), embedColor);
                sendMessageQueue(e, "Guild color mode set to: **" + WordUtils.capitalizeFully(embedColor.toDisplayString()) + "**");
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
                    sendMessageQueue(e, "Your privacy setting was set to: **" + WordUtils.capitalizeFully(privacyMode.toString()) + "**");
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
                    sendMessageQueue(e, "Will show botted acounts on the global leadearboard");
                } else {
                    sendMessageQueue(e, "Won't show botted acounts on the global leadearboard");
                }
                break;
            case NP:
                String[] split = value.trim().replaceAll(" +", " ").split("[|,& ]+");
                EnumSet<NPMode> modes = EnumSet.noneOf(NPMode.class);
                for (String mode : split) {
                    NPMode npMode = NPMode.valueOf(mode.replace("-", "_").toUpperCase());
                    modes.add(npMode);
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
            case TIMEZONE:
                break;
        }
    }
}
