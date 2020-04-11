package core.parsers;

import core.parsers.params.TimezoneParams;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.TimeZone;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class TimezoneParser extends Parser<TimezoneParams> {
    Predicate<String> errored = Pattern.compile("[+-]*00?:?0?0?[ ]*(gmt)?").asMatchPredicate();
    Predicate<String> gmtBased = Pattern.compile("[+-]*[ ]*(\\d{1,2}):\\d{1,2}").asMatchPredicate();

    public TimezoneParser(OptionalEntity... opts) {
        super(opts);
    }

    @Override
    void setUpOptionals() {
        this.opts.add(new OptionalEntity("--nam", "lol"));
    }

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected TimezoneParams parseLogic(MessageReceivedEvent e, String[] words) {
        String join = String.join(" ", words);
        if (gmtBased.test(join)) {
            join = "GMT" + join;
        }
        TimeZone timeZone = TimeZone.getTimeZone(join.toUpperCase());
        if (timeZone.equals(TimeZone.getTimeZone("+00:00")) && !errored.test(join.toLowerCase())) {
            sendError("Couldn't get a timezone!", e);
            return null;
        }
        return new TimezoneParams(e, e.getAuthor(), timeZone);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return null;
    }
}
