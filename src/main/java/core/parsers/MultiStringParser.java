package core.parsers;

import core.exceptions.LastFmException;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import javacutils.Pair;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class MultiStringParser<T extends CommandParameters> extends DaoParser<T> {
    public MultiStringParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    protected T parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        ParserAux parserAux = new ParserAux(words);
        User oneUser = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();
        LastFMData lastFMData = findLastfmFromID(oneUser, e);
        Pair<String[], Integer> integerPair = filterMessage(words, NumberParser.compile.asMatchPredicate(), Integer::parseInt, 2);
        int x = integerPair.second;
        if (x > 15) {
            sendError("Can't do more than 15", e);
            return null;
        }
        words = integerPair.first;
        if (words.length == 0) {
            return doSomethingNoWords(x, lastFMData, e);

        } else {
            String str = String.join(" ", getSubMessage(e.getMessage()));
            String[] split = str.split("(?<!\\\\)\\s*[|-]\\s*");
            Set<String> set = Set.of(split).stream().map(t ->
                    t.trim().replaceAll("\\\\(|-)", "$1")
            ).collect(Collectors.toSet());
            if (set.size() > 15) {
                sendError("Can't do more than 15", e);
                return null;
            }
            return doSomethingWords(lastFMData, e, set);

        }
    }

    protected abstract T doSomethingNoWords(int limit, LastFMData lastFMData, MessageReceivedEvent e) throws InstanceNotFoundException, LastFmException;

    protected abstract T doSomethingWords(LastFMData lastFMData, MessageReceivedEvent e, Set<String> strings);


}
