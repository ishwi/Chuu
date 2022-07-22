package test.commands;

import org.junit.jupiter.api.Test;
import test.commands.utils.CommandTest;
import test.commands.utils.OneLineUtils;

import java.util.regex.Pattern;

public class UpdateCommandTest extends CommandTest {
    @Override
    public String giveCommandName() {
        return "!update";
    }

    @Override
    @Test
    public void nullParserReturned() {
        Pattern compile = Pattern.compile("Successfully updated (.*) info !");
        OneLineUtils.testCommands(COMMAND_ALIAS, compile);
    }
}
