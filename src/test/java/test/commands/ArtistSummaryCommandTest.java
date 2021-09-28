package test.commands;

import org.junit.BeforeClass;
import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.EmbedWithFieldsUtils;
import test.commands.utils.FieldRowMatcher;
import test.commands.utils.TestResources;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ArtistSummaryCommandTest extends CommandTest {
    private static Pattern titlePattern;

    @BeforeClass
    public static void init() {
        titlePattern = Pattern.compile("Information about (.*)");

    }

    @Override
    public String giveCommandName() {
        return "!ai";
    }

    @Test
    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.artistParser(COMMAND_ALIAS);
    }

    @Test
    public void normalUsage() {

        //Usually the help message is sent to a private channel but i didnt find a way to get a private message from a marker so i opted to
        //send the message directly to the test channel

        List<FieldRowMatcher> fieldRowMatchers = new ArrayList<>();
        fieldRowMatchers.add(FieldRowMatcher.numberField(TestResources.testerJdaUsername + "'s plays:"));
        fieldRowMatchers.add(FieldRowMatcher.numberField("Listeners:"));
        fieldRowMatchers.add(FieldRowMatcher.numberField("Scrobbles:"));

        Pattern compile = Pattern.compile("(.*(\\[\\w+])(\\(.+\\))*.*)|^$|[\u200E]");

        fieldRowMatchers.add(new FieldRowMatcher("Tags:", compile));
        fieldRowMatchers.add(new FieldRowMatcher("Similars:", compile));
        fieldRowMatchers.add(FieldRowMatcher.matchesAnything("Bio:"));

        EmbedWithFieldsUtils
                .testEmbedWithFields("!ai radiohead", null, fieldRowMatchers, titlePattern, matcher -> matcher.group(1)
                        .equalsIgnoreCase("radiohead"));
        EmbedWithFieldsUtils
                .testEmbedWithFields("!ai saskure.uk", null, fieldRowMatchers, titlePattern, matcher -> matcher.group(1)
                        .equalsIgnoreCase("saskure.uk"));
    }

    @Test
    public void knownValuesUsage() {

        //Usually the help message is sent to a private channel but i didnt find a way to get a private message from a marker so i opted to
        //send the message directly to the test channel

        List<FieldRowMatcher> fieldRowMatchers = new ArrayList<>();
        fieldRowMatchers.add(FieldRowMatcher.numberFieldFromRange(TestResources.testerJdaUsername + "'s plays:", 282));
        fieldRowMatchers.add(FieldRowMatcher.numberFieldFromRange("Listeners:", 198273));
        fieldRowMatchers.add(FieldRowMatcher.numberFieldFromRange("Scrobbles:", 19443427));

        Pattern compile = Pattern.compile("(.*(\\[\\w+])(\\(.+\\))*.*)|^$");

        fieldRowMatchers.add(new FieldRowMatcher("Tags:", compile));
        fieldRowMatchers.add(new FieldRowMatcher("Similars:", compile));
        fieldRowMatchers.add(FieldRowMatcher.matchesAnything("Bio:"));

        EmbedWithFieldsUtils
                .testEmbedWithFields("!ai blackpink", null, fieldRowMatchers, titlePattern, matcher -> matcher.group(1)
                        .equalsIgnoreCase("blackpink"));
    }

}

