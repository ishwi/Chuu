package test.commands;

import core.Chuu;
import dao.exceptions.ChuuServiceException;
import org.junit.jupiter.api.Test;
import test.commands.utils.CommandTest;
import test.commands.utils.OneLineUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class InviteCommandTest extends CommandTest {
    @Override
    public String giveCommandName() {
        return "!invite";
    }

    @Override
    public void nullParserReturned() {

    }

    @Test
    public void normalFunctionality() {
        Properties properties = new Properties();
        try (InputStream in = Chuu.class.getResourceAsStream("/all.properties")) {
            properties.load(in);
        } catch (IOException e) {
            throw new ChuuServiceException(e);
        }
        Pattern pattern = Pattern.compile("Using the following link you can invite me to your server:\n" +
                "https://discordapp\\.com/oauth2/authorize\\?scope=bot&client_id=(\\d+){18}&permissions=(?:\\d+)");

        OneLineUtils.testCommands(COMMAND_ALIAS, pattern);
    }
}
