package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ChuuDataParams;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.Collections;
import java.util.List;

/**
 * returns: []; in 0 -> lastfmid, 1 -> discordId, 2... -> opts
 */
public class OnlyUsernameParser extends DaoParser<ChuuDataParams> {
    public OnlyUsernameParser(ChuuService dao) {
        super(dao);
    }

    public OnlyUsernameParser(ChuuService dao, OptionalEntity... opts) {
        super(dao);
        addOptional(opts);
    }

    @Override
    public ChuuDataParams parseSlashLogic(ContextSlashReceived ctx) throws InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        User user = InteractionAux.parseUser(e);
        var data = findLastfmFromID(user, ctx);
        return new ChuuDataParams(ctx, data);
    }

    @Override
    public ChuuDataParams parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {
        LastFMData data = atTheEndOneUser(e, subMessage);
        return new ChuuDataParams(e, data);
    }

    @Override
    public List<Explanation> getUsages() {
        return Collections.singletonList(new PermissiveUserExplanation());
    }
}
