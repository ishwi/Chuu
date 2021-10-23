package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.TimezoneParams;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TimezoneParser extends DaoParser<TimezoneParams> {
    private static final Map<String, String> mapZone = ZoneId.getAvailableZoneIds().stream()
            .collect(Collectors
                    .toMap(x -> TimeZone.getTimeZone(x).toZoneId()
                                    .normalized().getRules().getStandardOffset(Instant.now()).getId()
                            , x -> x, (x, y) -> x));

    private static final TimeZone gmt = TimeZone.getTimeZone("GMT");

    final Predicate<String> errored = Pattern.compile("[+-]*00?:?0?0?[ ]*(gmt)?").asMatchPredicate();
    final Pattern weekPattern = Pattern.compile("(?:UTC)?([+-])?(\\d{0,2})(:(\\d){2})?[ ]*(gmt)?", Pattern.CASE_INSENSITIVE);

    Predicate<String> gmtBased = Pattern.compile("[+-]*[ ]*(\\d{1,2}):\\d{1,2}").asMatchPredicate();

    public TimezoneParser(ChuuService chuuService, OptionalEntity... opts) {
        super(chuuService, opts);
    }

    @Override
    void setUpOptionals() {
    }

    @Override
    protected void setUpErrorMessages() {
        this.errorMessages.put(10, """
                Couldn't parse any timezone from the given message :(
                 The timezone can be written either as a abbreviate of the timezone(CET, PT...),the offset of the timezone  (+01:00, -12:00...) or trying to write a representative of the timezone using the following format (Europe/Brussels,America/Los Angeles...)
                 Refer to https://en.wikipedia.org/wiki/List_of_tz_database_time_zones for the full name of the timezones that are accepted.""");

    }

    @Override
    public TimezoneParams parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        return parseLogic(ctx, new String[]{ctx.e().getOption("timezone").getAsString()});
    }

    @Override
    protected TimezoneParams parseLogic(Context e, String[] words) throws InstanceNotFoundException {
        ParserAux parserAux = new ParserAux(words);
        User oneUser = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();
        String join = String.join(" ", words);
        if (join.isBlank()) {
            return new TimezoneParams(e, oneUser, null, true);
        }

        String id;
        if (join.length() <= 3) {
            join = join.toUpperCase(Locale.ROOT);
        }
        TimeZone timeZone = TimeZone.getTimeZone(join);
        if (timeZone.equals(gmt) && !errored.test(join)) {
            Set<String> zids = ZoneId.getAvailableZoneIds();
            String tzCityName = Normalizer.normalize(join, Normalizer.Form.NFKD)
                    .replaceAll("[^\\p{ASCII}-_ ]", "")
                    .replace(' ', '_');
            if (tzCityName.contains("_")) {
                String[] s = tzCityName.split("_");
                Optional<ZoneId> first = zids.stream()
                        .filter(zid -> zid.toLowerCase().startsWith(s[0]))
                        .map(ZoneId::of)
                        .findFirst();
                if (first.isPresent()) {
                    tzCityName = String.join("_", Arrays.copyOfRange(s, 1, s.length));
                }
            }
            String finalTzCityName = tzCityName;
            Optional<ZoneId> zone = zids.stream()
                    .filter(zid -> zid.toLowerCase().endsWith("/" + finalTzCityName.toLowerCase()))
                    .map(ZoneId::of)
                    .findFirst();
            if (zone.isPresent()) {
                id = zone.get().getId();
            } else {

                try {
                    Matcher matcher = weekPattern.matcher(join);
                    if (matcher.matches()) {
                        String appender = "";
                        if (matcher.group(1) == null) {
                            appender += '+';
                        } else {
                            appender += matcher.group(1);
                        }
                        appender += String.format("%02d", Integer.valueOf(matcher.group(2)));
                        if (matcher.group(3) == null) {
                            appender += ":00";
                        } else {
                            appender += matcher.group(3);
                        }
                        join = appender;
                    }
                    ZoneOffset of = ZoneOffset.of(join);
                    id = of.getId();

                    String a;
                    if ((a = mapZone.get(id)) != null) {
                        id = a;
                    } else {
                        sendError(getErrorMessage(10), e);
                        return null;
                    }
                } catch (DateTimeException ex) {
                    sendError(getErrorMessage(10), e);
                    return null;
                }
            }
            timeZone = TimeZone.getTimeZone(id);
        }

        return new TimezoneParams(e, oneUser, timeZone);
    }

    @Override
    public List<Explanation> getUsages() {
        String desc = "The timezone can be written either as a abbreviate of the timezone (CET, PT...), the offset of the timezone" +
                " (+01:00, -12:00...) or trying to write a representative of the timezone using the following format (Europe/Brussels,America/Los Angeles...)";
        OptionData optionData = new OptionData(OptionType.STRING, "timezone", StringUtils.abbreviate(desc, 100), true);
        ExplanationLine tz = new ExplanationLine("timezone", desc, optionData);
        Explanation timezone = () -> tz;

        return List.of(timezone);
    }

}
