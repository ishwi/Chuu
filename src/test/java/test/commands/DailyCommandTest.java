package test.commands;

import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.OneLineUtils;
import test.commands.utils.TestResources;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DailyCommandTest extends CommandTest {
    private static boolean test(Matcher matcher) {
        return

                (
                        //Case where user hasnt played anything
                        Long.parseLong(matcher.group(2)) == 0 && matcher.group(3)
                                .equals("s, really, 0! mins in the last 24 hours"))
                ||
                (//Case when user has played something
                        Long.parseLong(matcher.group(2)) != 0 && Long
                                                                         .parseLong(matcher.group(2)) == Long.parseLong(matcher.group(4)) * 60 + Long
                                .parseLong(matcher.group(5)) && Long.parseLong(matcher.group(6)) >= 0) && (Long
                                                                                                                   .parseLong(matcher.group(6)) == 1 && matcher.group(7).isEmpty() || (Long
                                                                                                                                                                                               .parseLong(matcher.group(6)) != 1 && matcher.group(7).equals("s")));
    }

    @Override
    public String giveCommandName() {
        return "!daily";
    }

    @Test
    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.onlyUsernameParser(COMMAND_ALIAS);
    }

    @Test
    public void normalUsageTest() {
        Pattern compile = Pattern
                .compile("(.*) played (\\d+) min(utes of music, \\((\\d)+:(\\d+) hours\\), listening to (\\d+) track(s)? in the last 24 hours|s, really, 0! mins in the last 24 hours)");

        OneLineUtils.testCommands(COMMAND_ALIAS, compile, //First words is always username
                //Case where user hasnt played anything
                //Case when user has played something
                DailyCommandTest::test);
        OneLineUtils.testCommands(COMMAND_ALIAS + " " + TestResources.ogJDA.getSelfUser()
                        .getAsMention(), compile, //First words is always username
                //Case where user hasnt played anything
                //Case when user has played something
                DailyCommandTest::test);

    }

    public static class SetCommandTest extends CommandTest {
        @Override
        public String giveCommandName() {
            return "!set";
        }

        @Test
        @Override
        public void nullParserReturned() {
            NullReturnParsersTest.setParser(COMMAND_ALIAS);
        }
    }
}
