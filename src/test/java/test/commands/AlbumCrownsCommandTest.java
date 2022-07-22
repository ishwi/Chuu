package test.commands;

import org.junit.jupiter.api.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.EmbedUtils;
import test.commands.utils.TestResources;

import java.util.regex.Pattern;

public class AlbumCrownsCommandTest extends CommandTest {


    @Override
    public String giveCommandName() {
        return null;
    }

    //No Parsers
    @Test
    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.onlyUsernameParser("!crowns");
        NullReturnParsersTest.onlyUsernameParser("!crownsal");
        NullReturnParsersTest.onlyUsernameParser("!unique");
        NullReturnParsersTest.twoUsersParser("!stolen");

    }


    @Test
    public void crowns() {

        String regex = "${header}'s crown(s)?";

        EmbedUtils.testLeaderboardEmbed("!crowns", EmbedUtils.descriptionArtistRegex, regex, false, Pattern
                .compile("You don't have any crown :'\\("));

        EmbedUtils.testLeaderboardEmbed("!crowns " + TestResources.ogJDA.getSelfUser()
                .getAsMention(), EmbedUtils.descriptionArtistRegex, regex, false, true, Pattern
                .compile("You don't have any crown :'\\("));
    }

    @Test
    public void globalCrowns() {

        String regex = "${header}'s global crown(s)?";

        EmbedUtils.testLeaderboardEmbed("!globalcrowns", EmbedUtils.descriptionArtistRegex, regex, false, Pattern
                .compile("You don't have any global crown :'\\("));

        EmbedUtils.testLeaderboardEmbed("!globalcrowns " + TestResources.ogJDA.getSelfUser()
                .getAsMention(), EmbedUtils.descriptionArtistRegex, regex, false, true, Pattern
                .compile("You don't have any global crown :'\\("));
    }

    @Test
    public void stolenCrowns() {

        Pattern noEmbeddedPattern = Pattern.compile("(.*) hasn't stolen anything from (.*)");

        Pattern noEmbeddedPattern2 = Pattern.compile("Sis, dont use the same person twice");

        String titleRegex = ".*?(?=Crowns stolen by )Top crowns stolen by .*";
        TestResources.insertCommonArtistWithPlays(Integer.MAX_VALUE);
        EmbedUtils.testLeaderboardEmbed("!stolen " + TestResources.ogJDA.getSelfUser()
                .getAsMention(), EmbedUtils.stolenRegex, titleRegex, false, false, noEmbeddedPattern);
        TestResources.insertCommonArtistWithPlays(1);
        EmbedUtils.testLeaderboardEmbed("!stolen " + TestResources.ogJDA.getSelfUser()
                .getAsMention(), EmbedUtils.stolenRegex, titleRegex, false, false, noEmbeddedPattern);

        EmbedUtils.testLeaderboardEmbed("!stolen " + TestResources.testerJDA.getSelfUser()
                .getAsMention(), EmbedUtils.stolenRegex, titleRegex, false, false, noEmbeddedPattern2);

    }


    @Test
    public void crownsAlbum() {
        //Empty

        Pattern noembededMessage = Pattern.compile("(.*) doesn't have any album crown :'\\(");
        Pattern noembededMessageLb = Pattern.compile("This guild has no registered users:\\(");

        String regex = "${header}'s album crown(?:s)?";
        String regexLB = "${header}'s Album Crowns leaderboard";
        EmbedUtils.testLeaderboardEmbed("!crownsal " + TestResources.ogJDA.getSelfUser()
                .getAsMention(), EmbedUtils.descriptionArtistAlbumRegex, regex, false, true, noembededMessage);
        EmbedUtils
                .testLeaderboardEmbed("!crownsalbumlb", EmbedUtils.descriptionArtistRegex, regexLB, true, noembededMessageLb);

        TestResources.dao
                .insertAlbumCrown(1, "Test Album That shoudnt Exists", TestResources.testerJDA
                        .getSelfUser().getIdLong(), TestResources.channelWorker.getGuild().getIdLong(), 199);
        //With something
        EmbedUtils
                .testLeaderboardEmbed("!crownsal", EmbedUtils.descriptionArtistAlbumRegex, regex, false, noembededMessage);
        EmbedUtils
                .testLeaderboardEmbed("!crownsalbumlb", EmbedUtils.descriptionArtistRegex, regexLB, true, noembededMessageLb);

        //Revert
        TestResources.dao
                .deleteAlbumCrown(TestResources.commonArtist, "Test Album That shoudnt Exists", TestResources.testerJDA
                        .getSelfUser().getIdLong(), TestResources.channelWorker.getGuild().getIdLong());
    }

    @Test
    public void uniques() {

        Pattern noEmbeddPattern = Pattern.compile("You have no Unique Artists :\\(");
        String regex = "${header}'s Top 10 unique Artists";
        EmbedUtils.testLeaderboardEmbed("!unique", EmbedUtils.descriptionArtistRegex, regex, false, noEmbeddPattern);
        EmbedUtils.testLeaderboardEmbed("!unique " + TestResources.ogJDA.getSelfUser()
                .getAsMention(), EmbedUtils.descriptionArtistRegex, regex, false, true, noEmbeddPattern);

    }

    @Test
    public void globalUniques() {

        Pattern noEmbeddPattern = Pattern.compile("You have no global Unique Artists :\\(");
        String regex = "${header}'s Top 10 global unique Artists";
        EmbedUtils.testLeaderboardEmbed("!globalunique", EmbedUtils.descriptionArtistRegex, regex, false, noEmbeddPattern);
        EmbedUtils.testLeaderboardEmbed("!globalunique " + TestResources.ogJDA.getSelfUser()
                .getAsMention(), EmbedUtils.descriptionArtistRegex, regex, false, true, noEmbeddPattern);

    }

    @Test
    public void obscurityLb() {
        Pattern noembededMessageLb = Pattern.compile("This guild has no registered users:\\(");

        EmbedUtils
                .testLeaderboardEmbed("!obscuritylb", EmbedUtils.descriptionArtistRegex, "${header}'s Obscurity points leaderboard", true, noembededMessageLb);
    }


    @Test
    public void scrobbledLb() {
        Pattern noembededMessageLb = Pattern.compile("This guild has no registered users:\\(");

        EmbedUtils
                .testLeaderboardEmbed("!scrobbledlb", EmbedUtils.descriptionArtistRegex, "${header}'s artist leaderboard", true, noembededMessageLb);
    }

    @Test
    public void uniqueLb() {
        Pattern noembededMessageLb = Pattern.compile("This guild has no registered users:\\(");

        EmbedUtils
                .testLeaderboardEmbed("!uniquelb", EmbedUtils.descriptionArtistRegex, "${header}'s Unique Artists leaderboard", true, noembededMessageLb);
    }


    @Test
    public void crownsLb() {
        Pattern noembededMessageLb = Pattern.compile("This guild has no registered users:\\(");

        EmbedUtils
                .testLeaderboardEmbed("!crownslb", EmbedUtils.descriptionArtistRegex, "${header}'s Crowns leaderboard", true, noembededMessageLb);
    }


}
