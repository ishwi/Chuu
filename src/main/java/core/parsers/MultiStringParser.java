package core.parsers;

import core.commands.Context;
import core.exceptions.LastFmException;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import javacutils.Pair;
import net.dv8tion.jda.api.entities.User;

import java.util.Set;
import java.util.stream.Collectors;

import static core.parsers.ParserAux.digitMatcher;

public abstract class MultiStringParser<T extends CommandParameters> extends DaoParser<T> {
    public MultiStringParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    protected T parseLogic(Context e, String[] words) throws InstanceNotFoundException, LastFmException {
        ParserAux parserAux = new ParserAux(words);
        User oneUser = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();
        LastFMData lastFMData = findLastfmFromID(oneUser, e);
        Pair<String[], Integer> integerPair = filterMessage(words, digitMatcher.asMatchPredicate(), Integer::parseInt, 2);
        int x = integerPair.second;
        if (x > 15) {
            sendError("Can't do more than 15", e);
            return null;
        }
        words = integerPair.first;
        if (words.length == 0) {
            return doSomethingNoWords(x, lastFMData, e);

        } else {
            String str = String.join(" ", getSubMessage(e));
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

    protected abstract T doSomethingNoWords(int limit, LastFMData lastFMData, Context e) throws InstanceNotFoundException, LastFmException;

    protected abstract T doSomethingWords(LastFMData lastFMData, Context e, Set<String> strings);


}
