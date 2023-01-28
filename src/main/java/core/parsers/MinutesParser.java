package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.MinutesParameters;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinutesParser extends Parser<MinutesParameters> {
    private static final Pattern timeRegexp = Pattern.compile("(\\d{1,4})\\s*(h(?:ours?)?|m(?:inutes?)?|s(?:econds?)?)?");

    @Override
    public MinutesParameters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) {
        CommandInteraction e = ctx.e();
        int seconds = Optional.ofNullable(e.getOption("seconds")).map(OptionMapping::getAsLong).map(Math::toIntExact).orElse(0);
        int minutes = Optional.ofNullable(e.getOption("minutes")).map(OptionMapping::getAsLong).map(Math::toIntExact).orElse(0);
        int hours = Optional.ofNullable(e.getOption("hours")).map(OptionMapping::getAsLong).map(Math::toIntExact).orElse(0);
        if (seconds == 0 && minutes == 0 && hours == 0) {
            sendError("You need to introduce the amount of seconds,minutes or hours you want to skip. Examples: `10 secs, 10 mins, 3:20, 10 mins 10 seconds, 10...", ctx);
            return null;
        }
        return new MinutesParameters(ctx, seconds, minutes, hours);
    }

    @Override
    protected MinutesParameters parseLogic(Context e, String[] words) {
        if (words.length == 0) {
            sendError("You need to introduce the amount of seconds,minutes or hours you want to skip. Examples: `10 secs, 10 mins, 3:20, 10 mins 10 seconds, 10...", e);
            return null;
        }
        String input = String.join(" ", words);
        Matcher matcher = timeRegexp.matcher(input);
        boolean matched = false;
        TemporalUnit hours = new TemporalUnit();
        TemporalUnit minutes = new TemporalUnit();
        TemporalUnit seconds = new TemporalUnit();
        List<Integer> unprocessed = new ArrayList<>();
        while (matcher.find()) {
            matched = true;
            int units = Integer.parseInt(matcher.group(1));
            String group = matcher.group(2);
            if (group == null) {
                unprocessed.add(units);
                continue;
            }
            if (group.startsWith("h")) {
                if (hours.proccessUnit(units)) {
                    sendError("You specified hours twice!", e);
                    return null;
                }
                continue;
            } else if (group.startsWith("m")) {
                if (minutes.proccessUnit(units)) {
                    sendError("You specified minutes twice!", e);
                    return null;
                }
                continue;
            } else if (group.startsWith("s")) {
                if (seconds.proccessUnit(units)) {
                    sendError("You specified seconds twice!", e);
                    return null;
                }
                continue;
            }
            unprocessed.add(units);
        }
        if (unprocessed.size() > 3) {
            sendError("You need to introduce the amount of seconds|minutes or hours you want to skip", e);
            return null;
        }
        Integer secondIndex = null;
        Integer minutesIndex = null;
        Integer hoursIndex = null;
        switch (unprocessed.size()) {
            case 1 -> secondIndex = 0;
            case 2 -> {
                secondIndex = 1;
                minutesIndex = 0;
            }
            case 3 -> {
                secondIndex = 2;
                minutesIndex = 1;
                hoursIndex = 0;
            }
        }
        if (secondIndex != null) {
            if (seconds.proccessUnit(unprocessed.get(secondIndex))) {
                sendError("You specified seconds twice!", e);
                return null;
            }
        }
        if (hoursIndex != null) {
            if (hours.proccessUnit(unprocessed.get(hoursIndex))) {
                sendError("You specified hours twice!", e);
                return null;
            }
        }
        if (minutesIndex != null) {
            if (minutes.proccessUnit(unprocessed.get(minutesIndex))) {
                sendError("You specified minutes twice!", e);
                return null;
            }
        }


        if (!matched || (seconds.unit == 0 && hours.unit == 0 && minutes.unit == 0)) {
            sendError("You need to introduce the amount of seconds|minutes or hours you want to skip", e);
            return null;
        }
        return new MinutesParameters(e, minutes.unit, seconds.unit, hours.unit);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(() -> new ExplanationLineType("seconds", "seconds to seek in", OptionType.INTEGER),
                () -> new ExplanationLineType("minutes", "minutes to seek in", OptionType.INTEGER),
                () -> new ExplanationLineType("hours", "hours to seek in", OptionType.INTEGER)
        );
    }

    private static class TemporalUnit {
        private int unit = 0;

        public TemporalUnit() {
        }

        public int getUnit() {
            return unit;
        }

        public boolean proccessUnit(int unit) {
            if (this.unit != 0) {
                return true;
            }
            this.unit = unit;
            return false;
        }
    }
}
