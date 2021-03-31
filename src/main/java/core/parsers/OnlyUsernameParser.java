package core.parsers;

import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public List<Explanation> getUsages() {
        return Collections.singletonList(new PermissiveUserExplanation());
    }
}
