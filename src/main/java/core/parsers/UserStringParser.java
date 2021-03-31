package core.parsers;

import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.UserStringParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

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
    protected UserStringParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException {
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
        return List.of(() -> new ExplanationLine("Search phrase", null), new StrictUserExplanation());
    }

}
