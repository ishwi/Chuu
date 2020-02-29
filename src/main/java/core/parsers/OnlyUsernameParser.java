package core.parsers;

import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

/**
 * returns: []; in 0 -> lastfmid, 1 -> discordId, 2... -> opts
 */
public class OnlyUsernameParser extends DaoParser {
    public OnlyUsernameParser(ChuuService dao) {
        super(dao);
    }

    public OnlyUsernameParser(ChuuService dao, OptionalEntity... strings) {
        super(dao);
        opts.addAll(Arrays.asList(strings));
    }

    public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {

        LastFMData data = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
        return new String[]{data.getName(), String.valueOf(data.getDiscordId())};
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *username*** \n" +
               "\t If the username is not specified it defaults to author's account\n";
    }
}
