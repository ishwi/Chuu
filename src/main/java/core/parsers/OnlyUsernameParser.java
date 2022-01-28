package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ChuuDataParams;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

import java.util.Collections;
import java.util.List;

/**
 * returns: []; in 0 -> lastfmid, 1 -> discordId, 2... -> opts
 */
public class OnlyUsernameParser extends DaoParser<ChuuDataParams> {
    private final boolean required;


    public OnlyUsernameParser(ChuuService dao, OptionalEntity... opts) {
        super(dao);
        addOptional(opts);
        this.required = false;
    }

    public OnlyUsernameParser(ChuuService dao, boolean required, OptionalEntity... opts) {
        super(dao);
        addOptional(opts);
        this.required = required;
    }

    @Override
    public ChuuDataParams parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws InstanceNotFoundException {
        CommandInteraction e = ctx.e();
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
        Explanation explanation = new PermissiveUserExplanation();
        if (required) {
            explanation = InteractionAux.required(explanation);
        }
        return Collections.singletonList(explanation);
    }
}
