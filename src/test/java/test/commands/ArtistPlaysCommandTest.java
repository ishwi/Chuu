package test.commands;

import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.OneLineUtils;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArtistPlaysCommandTest extends CommandTest {

    private static final Pattern responsePattern = Pattern.compile(
            ".+?(?=has scrobbled)has scrobbled (.*) (\\d+) (times|time)");

    private static final BiFunction<String, Integer, Predicate<Matcher>> matcher = (s, i) -> matcher -> {
        String groupName = matcher.group(1);
        int playCount = Integer.parseInt(matcher.group(2));
        String plural = playCount == 1 ? "time" : "times";
        return groupName.equalsIgnoreCase(s) && playCount >= i && plural.equals(matcher.group(3));
    };

    @Override
    public String giveCommandName() {
        return "!plays";
    }

    @Test
    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.artistParser(COMMAND_ALIAS);
    }

    @Test
    public void TestNormalPlays() {

        OneLineUtils.testCommands(COMMAND_ALIAS + " blackpink", responsePattern, ArtistPlaysCommandTest.matcher
                .apply("blackpink", 282));

        //Now Playing: Cant really tell the number of plays in advance
        OneLineUtils.testCommands(COMMAND_ALIAS, responsePattern, null);
    }

    @Test
    public void TestOnePlay() {
        OneLineUtils.testCommands(COMMAND_ALIAS + "  Will Philips", responsePattern, ArtistPlaysCommandTest.matcher
                .apply("Will Philips", 1));
    }

}
