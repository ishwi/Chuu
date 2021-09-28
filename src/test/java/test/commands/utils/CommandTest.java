package test.commands.utils;

import org.junit.ClassRule;
import org.junit.rules.TestRule;

public abstract class CommandTest {
    @ClassRule
    public static final TestRule res = TestResources.INSTANCE;
    public final String COMMAND_ALIAS;

    public CommandTest() {
        COMMAND_ALIAS = giveCommandName();
    }

    public abstract String giveCommandName();

    public abstract void nullParserReturned();


}
