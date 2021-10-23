package test.commands;

import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.ImageUtils;
import test.commands.utils.OneLineUtils;
import test.commands.utils.TestResources;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BandInfoCommandTest extends CommandTest {
    @Override
    public String giveCommandName() {
        return "!a";
    }

    @Test
    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.artistParser(COMMAND_ALIAS);
    }

    @Test
    public void normalTestCase() {
        ImageUtils.testImage(COMMAND_ALIAS + " blackpink", 1000, 1500, ".png");
    }

    @Test
    public void noPlaysOnArtistCase() {

        Pattern pattern = Pattern.compile("Error on (.*?)'s request:\nYou still haven't listened to (.*)");
        Predicate<Matcher> predicate = matcher1 ->
                matcher1.group(1).equals(TestResources.testerJdaUsername) &&
                        matcher1.group(2).equalsIgnoreCase("my bloody valentine");

        OneLineUtils.testCommands(COMMAND_ALIAS + " my bloody valentine", pattern, predicate);

        //Test with corrections
        OneLineUtils.testCommands(COMMAND_ALIAS + " my bloody valentien", pattern, predicate);

        //In previous call the correction was not on the dbs, but now it should be cached
        OneLineUtils.testCommands(COMMAND_ALIAS + " my bloody valentien", pattern, predicate);

    }


}
