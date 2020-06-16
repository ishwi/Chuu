package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.RYMRatingParams;
import dao.ChuuService;
import dao.entities.LastFMData;
import javacutils.Pair;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class RYMRatingParser extends DaoParser<RYMRatingParams> {

    private static final Pattern doublePatter = Pattern.compile("([1-9]([0-9]*))(?:.|,)?([50])?");

    public RYMRatingParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    protected RYMRatingParams parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        Predicate<String> stringPredicate = doublePatter.asMatchPredicate();
        Pair<String[], Double> doublePair = filterMessage(words, stringPredicate, x -> Double.valueOf(x.replaceAll(",", ".")), null);
        Double second = doublePair.second;
        Short rating;
        if (second != null) {
            if (second < 0) {
                sendError("The rating can only be interpreted in the scale 0-5 (with .5 decimals), 0-10 and 0-100", e);
                return null;
            }
            if ((second % 1 != 0) && second >= 5f) {
                sendError("The rating can only be interpreted in the scale 0-5 (with .5 decimals), 0-10 and 0-100", e);
                return null;
            }
            if (second <= 5f) {
                rating = (short) (second.intValue() * 2);
            } else if (second > 5f && second <= 10) {
                rating = (short) second.intValue();
            } else if (second > 10f && second <= 100f) {
                rating = (short) (second.intValue() / 10);
            } else {
                sendError("The rating can only be interpreted in the scale 0-5 (with .5 decimals), 0-10 and 0-100", e);
                return null;
            }
        } else {
            Pair<String[], Integer> pair = filterMessage(words, NumberParser.compile.asMatchPredicate(), Integer::valueOf, null);
            Integer second1 = pair.second;
            if (second1 != null) {
                if (second1 < 0) {
                    sendError("The rating can only be interpreted in the scale 0-5 (with .5 decimals), 0-10 and 0-100", e);
                    return null;
                }
                if (second1 <= 5) {
                    rating = (short) (second1 * 2);
                } else if (second1 <= 10) {
                    rating = (short) (second1.intValue());
                } else if (second1 <= 100) {
                    rating = (short) (second1 / 10);
                } else {
                    sendError("The rating can only be interpreted in the scale 0-5 (with .5 decimals), 0-10 and 0-100", e);
                    return null;
                }
            } else {
                rating = null;
            }
        }
        words = doublePair.first;
        LastFMData lastFMData = atTheEndOneUser(e, words);
        return new RYMRatingParams(e, lastFMData, rating);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *rating_score* *username* " +
                "\n\tIf username it's not provided it defaults to authors account, only ping and tag format (user#number)\n " +
                "\n\t The rating can only be interpreted in the scale 0-5 (with .5 decimals), 0-10 and 0-100";

    }
}
