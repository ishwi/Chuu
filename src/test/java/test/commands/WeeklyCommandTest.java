package test.commands;

import org.junit.Assert;
import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.EmbedUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeeklyCommandTest extends CommandTest {
    @Override
    public String giveCommandName() {
        return "!weekly";
    }

    @Test
    @Override
    public void nullParserReturned() {

        NullReturnParsersTest.onlyUsernameParser(COMMAND_ALIAS);
    }

    @Test
    public void normalWorkingTest() {

        Pattern pattern = Pattern
                .compile("((?:Mon|Tues|Wed(?:nes)?|Thur(?:s)?|Fri|Sat(?:ur)?|Sun)(?:day)?), (\\d{1,2})/(\\d{1,2}): (\\d+) minutes, \\((\\d+):(\\d+)h\\) on (\\d+) track(:?s)?");
        Predicate<Matcher> descriptionMatcher = matcher -> {
            List<String> daysOfWeek = Arrays
                    .asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
            Assert.assertTrue(daysOfWeek.contains(matcher.group(1)));

            long dayInNumber = Long.parseLong(matcher.group(2));
            long monthInNumber = Long.parseLong(matcher.group(3));

            //Case when user has played something
            long totalMinutes = Long.parseLong(matcher.group(4));
            long partialMinutes = Long.parseLong(matcher.group(6));
            long totalHours = Long.parseLong(matcher.group(5));
            long songs = Long.parseLong(matcher.group(7));
            String plural = (matcher.group(8));
            return
                    dayInNumber > 0 && dayInNumber <= 31
                    && monthInNumber > 0 && monthInNumber <= 12
                    && totalMinutes != 0
                    && totalMinutes == totalHours * 60 + partialMinutes
                    && songs > 0
                    && (songs == 1 && plural.isEmpty() || (songs != 1 && plural.equals("s")));
        };
        EmbedUtils.testEmbed(COMMAND_ALIAS, pattern, descriptionMatcher, "${header}'s week", false, false, null, null);
    }
}
