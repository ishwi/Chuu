package test.commands;

import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.ImageUtils;

public class NoiseGenreCommandTest extends CommandTest {
    @Override
    public String giveCommandName() {
        return "!genre";
    }

    @Test
    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.timerFrameParser(COMMAND_ALIAS);
    }

    @Test
    public void normalUsage() {
        ImageUtils.testImage(COMMAND_ALIAS + " s", false, 600, 800, 40, ".png");

    }
}
