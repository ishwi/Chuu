package test.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.Assert;
import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.TestResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static test.commands.utils.TestResources.channelWorker;

public class SetCommandTest extends CommandTest {


    @Override
    public String giveCommandName() {
        return "!set";
    }

    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.setParser(COMMAND_ALIAS);
    }


    @Test
    public void normalUsage() {
        String username = TestResources.testerJdaUsername;
        String s = "Changing your username, might take a while";
        String s1 = username + " , you are good to go!";
        String s2 = username + " has set their last FM name \n Updating your library , wait a moment";
        String s3 = "Finished updating " + username + " library, you are good to go!";
        String s4 = "The provided username doesn't exist on last.fm, choose another one";
        String s5 = "Error  updating" + username + "'s  library, try to use the !update command!";
        String s6 = "That username is already registered in this server sorry";

        List<String> valid_set = Arrays.asList(s3, s2);

        List<String> invalid_name = Arrays.asList(s, s4);

        testMessage(COMMAND_ALIAS + " asihudbaiuxcbniuabsciuyabsiudb", 2, invalid_name);

        testMessage(COMMAND_ALIAS + " pablopita", 2, valid_set);

        testMessage(COMMAND_ALIAS + " pablopita", 1, Collections.singletonList(s6));

    }

    private void testMessage(String command, int numberOfMessages, List<String> messages) {
        assert numberOfMessages == messages.size();
        long id = channelWorker.sendMessage(command).complete().getIdLong();
        await().atMost(45, TimeUnit.SECONDS).until(() ->
        {
            MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
            return complete.getRetrievedHistory().size() == numberOfMessages;
        });
        List<Message> retrievedHistory = new ArrayList<>(channelWorker.getHistoryAfter(id, 20).complete()
                .getRetrievedHistory());
        retrievedHistory.removeIf(x -> messages.contains(x.getContentStripped()));
        Assert.assertTrue(retrievedHistory.isEmpty());
    }
}
