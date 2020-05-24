package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.GuildConfigParser;
import core.parsers.Parser;
import core.parsers.params.GuildConfigParams;
import core.parsers.params.GuildConfigType;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
        switch (config) {
            case CROWNS_THRESHOLD:
                int threshold = Integer.parseInt(value);
                getService().updateGuildCrownThreshold(e.getGuild().getIdLong(), threshold);
                sendMessageQueue(e, "Successfully updated the crown threshold to " + threshold);
                break;
            case ADDITIONAL_CHART_INFO:
                boolean defaultChartInfo = Boolean.parseBoolean(value);
                getService().updateGuildDefaultChart(e.getGuild().getIdLong(), defaultChartInfo);
                if (defaultChartInfo) {
                    sendMessageQueue(e, "Now charts in this server will have additional info shown in their charts");
                } else {
                    sendMessageQueue(e, "Now chart in this server by default won't have additional info shown in their charts");
                }
                break;
        }
    }
}
