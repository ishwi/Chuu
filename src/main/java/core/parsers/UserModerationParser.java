package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ChuuDataParams;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Collections;
import java.util.List;

public class UserModerationParser extends DaoParser<ChuuDataParams> {
    public UserModerationParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    public ChuuDataParams parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws InstanceNotFoundException {
        var str = ctx.e().getOption("lastfm-name").getAsString();
        return parseLogic(ctx, new String[]{str});
    }

    @Override
    protected ChuuDataParams parseLogic(Context e, String[] words) throws InstanceNotFoundException {
        if (words.length != 1) {
            sendError("Need a Discord Id or a lastfm name", e);
            return null;
        }
        if (ParserAux.discordId.matcher(words[0]).matches()) {
            long id = Long.parseLong(words[0]);
            LastFMData lastFMData = dao.findLastFMData(id);
            return new ChuuDataParams(e, lastFMData);
        } else {
            LastFMData byLastfmName = dao.findByLastfmName(words[0]);
            return new ChuuDataParams(e, byLastfmName);
        }
    }

    @Override
    public List<Explanation> getUsages() {
        return Collections.singletonList(InteractionAux.required(() -> new ExplanationLineType("lastfm-name", "lfm name of user to flag as botted", OptionType.STRING)));
    }

}
