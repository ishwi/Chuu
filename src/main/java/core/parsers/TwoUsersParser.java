package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.TwoUsersExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.TwoUsersParamaters;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

import java.util.List;

public class TwoUsersParser extends DaoParser<TwoUsersParamaters> {
    public TwoUsersParser(ChuuService dao) {
        super(dao);
    }

    public TwoUsersParser(ChuuService dao, OptionalEntity... opts) {
        super(dao);
        addOptional(opts);
    }

    @Override
    public TwoUsersParamaters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {

        CommandInteraction event = ctx.e();
        User oneUser = event.getOption(TwoUsersExplanation.NAME).getAsUser();
        if (!ctx.isFromGuild() && oneUser.getIdLong() != ctx.getAuthor().getIdLong()) {
            sendError("Can't get two different users on DM's", ctx);
            return null;
        }
        return new TwoUsersParamaters(ctx, findLastfmFromID(ctx.getAuthor(), ctx), findLastfmFromID(oneUser, ctx));
    }

    public TwoUsersParamaters parseLogic(Context e, String[] words) throws InstanceNotFoundException {
        String[] message = getSubMessage(e);

        if (message.length == 0) {
            sendError(getErrorMessage(5), e);
            return null;
        }
        ParserAux parserAux = new ParserAux(words, isExpensiveSearch());
        LastFMData[] datas = parserAux.getTwoUsers(dao, words, e);
        // words = parserAux.getMessage();
        if (datas == null) {
            sendError("Couldn't get two users", e);
            return null;
        }
        if (!e.isFromGuild() && (datas[0].getDiscordId() != datas[1].getDiscordId())) {
            sendError("Can't get two different users on DM's", e);
            return null;
        }
        return new TwoUsersParamaters(e, datas[0], datas[1]);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new TwoUsersExplanation());
    }


    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(5, "Need at least one username");
        errorMessages.put(-1, "Mentioned user is not registered");


    }
}
