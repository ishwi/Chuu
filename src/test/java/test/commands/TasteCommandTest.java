package test.commands;

import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.ImageUtils;
import test.commands.utils.OneLineUtils;
import test.commands.utils.TestResources;

import java.util.regex.Pattern;

public class TasteCommandTest extends CommandTest {
    @Override
    public String giveCommandName() {
        return "!taste";
    }

    @Test
    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.twoUsersParser(COMMAND_ALIAS);

    }

    @Test
    public void normalUsage() {
        //
        TestResources.deleteCommonArtists();

        //No Common artists
        OneLineUtils.testCommands(COMMAND_ALIAS + " " + TestResources.ogJDA.getSelfUser()
                .getAsMention(), Pattern.compile("You don't share any artist :\\("), null);

        //One user as Mention
        TestResources.insertCommonArtistWithPlays(1);
        ImageUtils.testImage(COMMAND_ALIAS + " " + TestResources.ogJDA.getSelfUser().getAsMention(), 500, 600, ".png");

        //Two users as mentions
        ImageUtils.testImage(COMMAND_ALIAS + " " + TestResources.ogJDA.getSelfUser()
                .getAsMention() + " " + TestResources.testerJDA.getSelfUser()
                                     .getAsMention(), 500, 600, ".png");

        TestResources.deleteCommonArtists();
    }
}
