package test.commands.utils;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestResources.class)
public abstract class CommandTest {

    public final String COMMAND_ALIAS;

    public CommandTest() {
        COMMAND_ALIAS = giveCommandName();
    }

    public abstract String giveCommandName();

    public abstract void nullParserReturned();


}
