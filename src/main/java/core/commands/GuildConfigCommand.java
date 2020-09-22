package core.commands;

import com.google.common.collect.Sets;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.GuildConfigParser;
import core.parsers.Parser;
import core.parsers.params.GuildConfigParams;
import core.parsers.params.GuildConfigType;
import core.parsers.params.NPMode;
import dao.ChuuService;
import dao.entities.ChartMode;
import dao.entities.RemainingImagesMode;
import dao.entities.WhoKnowsMode;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.text.WordUtils;

import java.util.EnumSet;
import java.util.List;

public class GuildConfigCommand extends ConcurrentCommand<GuildConfigParams> {
    public GuildConfigCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<GuildConfigParams> getParser() {
        return new GuildConfigParser(getService());
    }

    @Override
    public String getDescription() {
        return "Configuration per server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("serverconfiguration", "serverconfig", "sconfig");
    }

    @Override
    public String getName() {
        return "Server Configuration";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        GuildConfigParams parse = this.parser.parse(e);
        if (parse == null) {
            return;
        }
        GuildConfigType config = parse.getConfig();
        String value = parse.getValue();
        boolean cleansing = value.equalsIgnoreCase("clear");
        switch (config) {
            case CROWNS_THRESHOLD:
                int threshold = Integer.parseInt(value);
                getService().updateGuildCrownThreshold(e.getGuild().getIdLong(), threshold);
                sendMessageQueue(e, "Successfully updated the crown threshold to " + threshold);
                break;
            case CHART_MODE:
                ChartMode chartMode;
                if (cleansing) {
                    chartMode = null;
                } else {
                    chartMode = ChartMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                getService().setServerChartMode(e.getGuild().getIdLong(), chartMode);
                if (cleansing) {
                    sendMessageQueue(e, "Now all charts are back to the default");
                } else {
                    sendMessageQueue(e, "Server chart mode set to: **" + WordUtils.capitalizeFully(chartMode.toString()) + "**");
                }
                break;
            case WHOKNOWS_MODE:
                WhoKnowsMode whoKnowsMode;
                if (cleansing) {
                    whoKnowsMode = null;
                } else {
                    whoKnowsMode = WhoKnowsMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                getService().setServerWhoknowMode(e.getGuild().getIdLong(), whoKnowsMode);
                if (cleansing) {
                    sendMessageQueue(e, "Now your who knows are back to the default");
                } else {
                    sendMessageQueue(e, "Who Knows mode set to: **" + WordUtils.capitalizeFully(whoKnowsMode.toString()) + "**");
                }
                break;
            case REMAINING_MODE:
                RemainingImagesMode remainingImagesMode;
                if (cleansing) {
                    remainingImagesMode = null;
                } else {
                    remainingImagesMode = RemainingImagesMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                getService().setRemainingImagesModeServer(e.getGuild().getIdLong(), remainingImagesMode);
                if (!cleansing) {
                    sendMessageQueue(e, "The mode of the remaining image commands was set to: **" + WordUtils.capitalizeFully(remainingImagesMode.toString()) + "**");
                } else {
                    sendMessageQueue(e, "The mode of the remaining image commands to the default");
                }
                break;
            case NP:
                String[] split = value.trim().replaceAll(" +", " ").split("[|,& ]+");
                EnumSet<NPMode> modes = EnumSet.noneOf(NPMode.class);
                for (String mode : split) {
                    if (mode.equalsIgnoreCase("CLEAR")) {
                        modes = EnumSet.of(NPMode.UNKNOWN);
                        break;
                    }
                    NPMode npMode = NPMode.valueOf(mode.replace("-", "_").toUpperCase());
                    modes.add(npMode);
                }
                if (modes.size() > 15) {
                    sendMessageQueue(e, "You can't set more than 15 as a default for the server");
                } else {
                    getService().setServerNPModes(e.getGuild().getIdLong(), modes);
                    String strModes = NPMode.getListedName(modes);
                    if (Sets.difference(modes, EnumSet.of(NPMode.UNKNOWN)).isEmpty()) {
                        sendMessageQueue(e, "Successfully cleared the server config");
                    } else {
                        sendMessageQueue(e, String.format("Successfully changed the server config to the following %s: %s", CommandUtil.singlePlural(modes.size(), "mode", "modes"), strModes));
                    }
                }
                break;
        }
    }
}
