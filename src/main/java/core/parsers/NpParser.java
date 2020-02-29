package core.parsers;

import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NpParser extends DaoParser {
    public NpParser(ChuuService dao) {
        super(dao);
    }

    public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        LastFMData data = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
        return new String[]{data.getName(), String.valueOf(data.getDiscordId())};
    }

    @Override
    public String getUsageLogic(String commandName) {
		return "**" + commandName + " *username***\n" +
               "\t If the username is not specified it defaults to authors account\n";
	}
}
