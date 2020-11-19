package test.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.CommandUtil;
import org.junit.Before;
import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

public class GlobalArtistCommand extends CommandTest {
    private String artistUrl;
    private String commonArtist;
    private ArrayList<FieldRowMatcher> basicList;
    private final BiFunction<String, List<FieldRowMatcher>, EmbedTester> artistgenerator = (artist, frm) ->
    {
        EmbedTesterBuilder embedTesterBuilder = new EmbedTesterBuilder(COMMAND_ALIAS + " " + artist);
        Pattern titlePattern = Pattern.compile("Who knows (.*) globally\\?");
        return embedTesterBuilder
                .titlePattern(titlePattern)
                .fieldRowMatch(frm)
                .build();
    };
    private final Function<List<FieldRowMatcher>, EmbedTester> generator = fieldRowMatchers -> this.artistgenerator.apply(commonArtist, fieldRowMatchers);

    @Override
    public String giveCommandName() {
        return "!global";
    }

    @Before
    public void setUp() throws Exception {
        ConcurrentLastFM newInstance = LastFMFactory.getNewInstance();
        DiscogsApi discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        Spotify spotify = SpotifySingleton.getInstance();
        this.commonArtist = TestResources.commonArtist;
        artistUrl = CommandUtil
                .getArtistImageUrl(TestResources.dao, TestResources.commonArtist, newInstance, discogsApi, spotify);
        this.basicList = new ArrayList<>();
        basicList.add(FieldRowMatcher.numberField("Total Listeners:"));
        basicList.add(FieldRowMatcher.numberField("Total Plays:"));

    }

    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.artistAlbumParser(COMMAND_ALIAS);
    }

    @Test
    public void normal() {
        String commonArtist = TestResources.commonArtist;
        TestResources.insertCommonArtistWithPlays(100000);

        ArrayList<FieldRowMatcher> copiedList1 = new ArrayList<>(basicList);

        copiedList1.add(FieldRowMatcher.numberField("Plays to rank up:"));
        copiedList1.add(new FieldRowMatcher("Position:", Pattern.compile("(\\d+)/(\\d+)"), matcher -> Integer.parseInt(matcher.group(1)) == 2));
        copiedList1.add(FieldRowMatcher.numberField("Your Plays:"));

        generator.apply(copiedList1).GeneralFunction();

        TestResources.deleteCommonArtists();
        TestResources.insertCommonArtistWithPlays(1);
        ArrayList<FieldRowMatcher> copiedList2 = new ArrayList<>(basicList);
        copiedList2.add(new FieldRowMatcher("Position:", Pattern.compile("(\\d+)/(\\d+)"), matcher -> Integer.parseInt(matcher.group(1)) == 1));
        copiedList2.add(FieldRowMatcher.numberField("Your Plays:"));
        generator.apply(copiedList2).GeneralFunction();

    }

    @Test
    public void unknownArtist() {
        OneLineUtils.testCommands(COMMAND_ALIAS + " Unkown Artist ", Pattern.compile("No one knows Unkown Artist"));
    }

    @Test
    public void noPlaysOnrKnownArtist() {
        TestResources.insertOnlyKnownSecond("OnlyKnownSecond", 1000);
        artistgenerator.apply("OnlyKnownSecond", basicList).GeneralFunction();
        TestResources.deleteOnlyKnownSecond();

    }
}
