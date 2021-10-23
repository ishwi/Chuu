package test.commands;

import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.ImageUtils;
import test.commands.utils.OneLineUtils;

import java.util.regex.Pattern;

public class AotyCommandTest extends CommandTest {
    @Override
    public String giveCommandName() {
        return "!aoty";
    }

    @Test
    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.chartFromYearParser(COMMAND_ALIAS);
    }

    @Test
    public void normalWorking() {
        ImageUtils
                .testImageWithPreWarningDeletable(COMMAND_ALIAS + " 1x1", "Will take a while", false, 300, 300, 70, ".png");

    }

    @Test
    public void failingCases() {
        OneLineUtils.testCommands(COMMAND_ALIAS + " 1x1 --nolimit", Pattern
                .compile("Error on (?:.*)'s request:\n" +
                        "Cant use a size for the chart if you specify the --nolimit flag!"));

        Pattern errorPattern = Pattern.compile("Error on (?:.*)'s request:\n" +
                "0 is not a valid value for a chart!");

        OneLineUtils.testCommands(COMMAND_ALIAS + " 1x0", errorPattern);

    }
}
