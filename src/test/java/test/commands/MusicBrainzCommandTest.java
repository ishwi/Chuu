package test.commands;

import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.ImageUtils;
import test.commands.utils.OneLineUtils;

import java.util.regex.Pattern;

public class MusicBrainzCommandTest extends CommandTest {
    @Override
    public String giveCommandName() {
        return "!releaseyear";
    }

    @Test
    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.chartFromYearParser(COMMAND_ALIAS);
    }

    @Test
    public void normalUsage() {
        ImageUtils.testImage(COMMAND_ALIAS, true, Integer.MAX_VALUE, Integer.MAX_VALUE, 75, ".png", ".jpg");

        Pattern compile = Pattern.compile("Dont have any (\\d{4}) album in your top (\\d+) albums");

        OneLineUtils
                .testCommands(COMMAND_ALIAS + " a 1876 ", compile, matcher -> Integer.parseInt(matcher.group(1)) == 1876 && Integer.parseInt(matcher.group(2)) == 150, 65);


    }
}
