package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.UserStringParameters;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;
import java.util.Optional;

public class UserStringParser extends DaoParser<UserStringParameters> {
    private final boolean allowEmpty;

    public UserStringParser(ChuuService dao, boolean allowEmpty, OptionalEntity... opts) {
        super(dao, opts);
        this.allowEmpty = allowEmpty;
    }

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    public UserStringParameters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        CommandInteraction e = ctx.e();
        User user = InteractionAux.parseUser(e);
        OptionMapping option = e.getOption("search-phrase");
        if (!allowEmpty && option == null) {
            sendError("Need at least one word!", ctx);
            return null;
        }
        return new UserStringParameters(ctx, findLastfmFromID(user, ctx), Optional.ofNullable(option).map(OptionMapping::getAsString).orElse(""));
    }


    @Override
    protected UserStringParameters parseLogic(Context e, String[] words) throws InstanceNotFoundException {
        ParserAux parserAux = new ParserAux(words);
        User oneUser = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();
        LastFMData lastfmFromID = findLastfmFromID(oneUser, e);
        String join = String.join(" ", words);
        if (!allowEmpty && join.isBlank()) {
            sendError("Need at least one word!", e);
            return null;
        }
        return new UserStringParameters(e, lastfmFromID, join);
    }

    @Override
    public List<Explanation> getUsages() {
        Explanation explanation = () -> new ExplanationLineType("search-phrase", "What you want to search for", OptionType.STRING);
        if (!allowEmpty) {
            explanation = InteractionAux.required(explanation);
        }
        return List.of(explanation, new StrictUserExplanation());
    }

}
