package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.RatingExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.RYMRatingParams;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class RYMRatingParser extends DaoParser<RYMRatingParams> {

    private static final Pattern doublePatter = Pattern.compile("(([1-9]([0-9]*))(?:.|,)?([50])?|0[,.]5)");

    public RYMRatingParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    public RYMRatingParams parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws InstanceNotFoundException {
        CommandInteraction e = ctx.e();
        Short rating = Optional.ofNullable(e.getOption(RatingExplanation.NAME)).map(OptionMapping::getAsLong).map(Long::shortValue).orElse(null);
        User user = InteractionAux.parseUser(e);
        return new RYMRatingParams(ctx, findLastfmFromID(user, ctx), rating);
    }

    @Override
    protected RYMRatingParams parseLogic(Context e, String[] words) throws InstanceNotFoundException {
        Predicate<String> stringPredicate = doublePatter.asMatchPredicate();
        Pair<String[], Double> doubleFilter = filterMessage(words, stringPredicate, x -> Double.valueOf(x.replaceAll(",", ".")), null);
        Double decimalRating = doubleFilter.getRight();
        Short rating;
        if (decimalRating != null) {
            if (decimalRating < 0) {
                sendError("The rating can only be interpreted in the scale 0-5 (with .5 decimals), 0-10 and 0-100", e);
                return null;
            }
            if ((decimalRating % 1 != 0) && decimalRating >= 5f) {
                sendError("The rating can only be interpreted in the scale 0-5 (with .5 decimals), 0-10 and 0-100", e);
                return null;
            }
            if (decimalRating <= 5f) {
                rating = (short) (decimalRating.intValue() * 2);
            } else if (decimalRating > 5f && decimalRating <= 10) {
                rating = (short) decimalRating.intValue();
            } else if (decimalRating > 10f && decimalRating <= 100f) {
                rating = (short) (decimalRating.intValue() / 10);
            } else {
                sendError("The rating can only be interpreted in the scale 0-5 (with .5 decimals), 0-10 and 0-100", e);
                return null;
            }
        } else {
            Pair<String[], Integer> intFilter = filterMessage(words, ParserAux.digitMatcher.asMatchPredicate(), Integer::valueOf, null);
            Integer numericRating = intFilter.getRight();
            if (numericRating != null) {
                if (numericRating < 0) {
                    sendError("The rating can only be interpreted in the scale 0-5 (with .5 decimals), 0-10 and 0-100", e);
                    return null;
                }
                if (numericRating <= 5) {
                    rating = (short) (numericRating * 2);
                } else if (numericRating <= 10) {
                    rating = (short) (numericRating.intValue());
                } else if (numericRating <= 100) {
                    rating = (short) (numericRating / 10);
                } else {
                    sendError("The rating can only be interpreted in the scale 0-5 (with .5 decimals), 0-10 and 0-100", e);
                    return null;
                }
            } else {
                rating = null;
            }
        }
        words = doubleFilter.getLeft();
        LastFMData lastFMData = atTheEndOneUser(e, words);
        return new RYMRatingParams(e, lastFMData, rating);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new RatingExplanation(), new PermissiveUserExplanation());
    }


}
