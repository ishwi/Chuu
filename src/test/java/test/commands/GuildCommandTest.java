package test.commands;

import org.junit.Test;
import test.commands.utils.CommandTest;
import test.commands.utils.ImageUtils;

public class GuildCommandTest extends CommandTest {
    @Override
    public String giveCommandName() {
        return "!guild";
    }

    @Override
    public void nullParserReturned() {
    }

    @Test
    public void normalUsage() {
        ImageUtils.testImage(COMMAND_ALIAS, 1500, 1500, ".png");
    }
}
