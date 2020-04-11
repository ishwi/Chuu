package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

/**
 * returns: []; in 0 -> lastfmid, 1 -> discordId, 2... -> opts
 */
public class OnlyUsernameParser extends DaoParser<ChuuDataParams> {
    public OnlyUsernameParser(ChuuService dao) {
        super(dao);
    }

    public OnlyUsernameParser(ChuuService dao, OptionalEntity... strings) {
        super(dao);
        opts.addAll(Arrays.asList(strings));
    }

    @Override
    public ChuuDataParams parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        LastFMData data = atTheEndOneUser(e, subMessage);
        return new ChuuDataParams(e, data);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *username*** \n" +
               "\t If the username is not specified it defaults to author's account\n";
    }
}
