package test.commands;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.OneLineUtils;
import test.commands.utils.TestResources;

import java.util.regex.Pattern;

public class RandomAlbumCommandTest extends CommandTest {
    @Override
    public String giveCommandName() {
        return "!random";
    }

    @Test
    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.randomAlbumParser(COMMAND_ALIAS);
    }

    @Test
    public void normalUSage() {
        Pattern emptyResponse = Pattern.compile("The pool of urls was empty, add one first!");
        Pattern oneResponse = Pattern.compile("(.*?), here's your random recommendation\n" +
                "Posted by: (.*?)\n" +
                "Link: (.*)");
        Pattern addedUrl = Pattern.compile("Successfully added (.*)'s link to the pool");
        Pattern alreadyOnPool = Pattern.compile("The provided url: (.*) was already on the pool");
        String url = "https://www.youtube.com/watch?v=iH0kfH04U68";

        //No url on pool
        OneLineUtils.testCommands(COMMAND_ALIAS, emptyResponse);

        //Adding one url to the pool
        OneLineUtils
                .testCommands(COMMAND_ALIAS + " " + url, addedUrl, matcher -> matcher
                        .group(1).equals(TestResources.testerJdaUsername));

        //Getting that url from the pool
        OneLineUtils.testCommands(COMMAND_ALIAS, oneResponse, matcher -> matcher.group(1)
                .equals("@" + TestResources.testerJdaUsername) &&
                matcher.group(2).equals(TestResources.testerJDA.getSelfUser().getName()) &&
                matcher.group(3).equals(url));

        //Cant re-add the same url
        OneLineUtils
                .testCommands(COMMAND_ALIAS + " " + url, alreadyOnPool, matcher -> matcher
                        .group(1).equals(url));

    }

    @AfterEach
    public void tearDown() {
        TestResources.dao.truncateRandomPool();
    }
}
