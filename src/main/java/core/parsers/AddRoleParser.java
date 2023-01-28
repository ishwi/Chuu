package core.parsers;

import core.commands.Context;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.AddRoleParameters;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.beryx.awt.color.ColorFactory;

import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddRoleParser extends Parser<AddRoleParameters> {
    private final Pattern fromTo = Pattern.compile("(\\d+) ?(?:to|:|->)? ?(\\d+)");

    @Override
    protected AddRoleParameters parseLogic(Context e, String[] words) {
        String all = String.join(" ", words);
        Matcher matcher = fromTo.matcher(all);
        int first;
        int second;
        if (matcher.find()) {
            first = Integer.parseInt(matcher.group(1));
            second = Integer.parseInt(matcher.group(2));
            words = all.replace(matcher.group(0), "").split(" ");
        } else {
            sendError("Need at least a start and an end number", e);
            return null;
        }

        if (first < 0 || second < 0) {
            sendError("Needs to be greater than 0", e);
            return null;
        }
        if (first > second) {
            sendError("Start of the range needs to be smaller than end of the range", e);
            return null;
        }

        Pair<String[], Color> colorPair = filterMessage(words, t -> {

            try {
                ColorFactory.valueOf(t);
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }, ColorFactory::valueOf, null);
        Color color = colorPair.getRight();
        if (color == null) {
            sendError("You need to provide a colour", e);
            return null;
        }

        words = colorPair.getLeft();
        String rest = String.join(" ", words);
        if (StringUtils.isBlank(rest)) {
            sendError("You need to provide a role name!", e);
            return null;
        }
        return new

                AddRoleParameters(e, rest, color, first, second);

    }

    @Override
    public List<Explanation> getUsages() {
        return null;
    }
}
