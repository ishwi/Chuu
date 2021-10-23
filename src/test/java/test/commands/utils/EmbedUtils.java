package test.commands.utils;

import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Before;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmbedUtils {
    public static final Pattern descriptionArtistRegex = Pattern.compile(
            "(\\d+)" + //Indexed list *captured
                    "\\. \\[(?:[^\\[\\]]+)]\\((?:[^)]+)\\)" + //Markdown link
                    "(?=(?: -|:))(?: -|:) " + //anything until a ":" or a " -"
                    "(\\d+) " + //count of the description *captured
                    "(play(?:s)?|(?:album )?crown(?:s)?|obscurity points|artist(?:s)?|unique artist(?:s)?)");
    //ending
    public static final Pattern descriptionArtistRegexNoMarkDownLink = Pattern.compile(
            "(\\d+)" + //Indexed list *captured
                    "\\. (?:.*) [\\-:] " + // aristName
                    "(\\d+) " + //count of the description *captured
                    "(play(?:s)?|(?:album )?crown(?:s)?|obscurity points|artist(?:s)?|unique artist(?:s)?)");
    public static final Pattern stolenRegex = Pattern.compile(
            "(\\d+)" + //Indexed list *captured
                    "\\. \\[(?:[^\\[\\]]+)]\\((?:[^)]+)\\)" + //Markdown link
                    "(?= : )(?: : )" + //anything until a ":"
                    "(\\d+)" + //your plays
                    "(?: -> )(?:\\d+)"); //Separator and other user plays
    public static final Pattern descriptionArtistAlbumRegex = Pattern.compile(
            "(\\d+)\\. " + //digit
                    "\\[(?:[^\\[\\]]+)]\\((?:[^)]+)\\)" + //markdown url
                    "(?= - ) - (\\d+) play(?:s)?"); /// remaining
    private static String serverThumbnail;
    private static String testerJDAThumbnail;
    private static String ogJDAThumbnail;
    public Function<String, String> getArtistThumbnail = (artistName) ->
            TestResources.dao.getArtistUrl(artistName);

    public static void testLeaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, String artistThumbnail, Pattern NoEmbededPattern) {
        testLeaderboardEmbed(command, descriptionRegex, titleRegex, isLeaderboard, false, artistThumbnail, NoEmbededPattern);
    }

    public static void testLeaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, boolean hasPing, String artistThumbnail, Pattern NoEmbededPattern) {

        Predicate<Matcher> matcherBooleanFunction = (Matcher matcher) ->
                Long.parseLong(matcher.group(1)) >= 0 && Long.parseLong(matcher.group(2)) >= 0;

        testEmbed(command, descriptionRegex, matcherBooleanFunction, titleRegex, isLeaderboard, hasPing, artistThumbnail, NoEmbededPattern);
    }

    public static void testEmbed(String command, Pattern descriptionRegex, Predicate<Matcher> matcherDescription, String titleRegex, boolean isLeaderboard, boolean hasPing, String artistThumbnail, Pattern NoEmbededPattern) {
        String header;
        Optional<Member> first;
        if (hasPing) {
            first = TestResources.channelWorker.getMembers().stream()
                    .filter(x -> command.contains(x.getAsMention())).findFirst();
        } else {
            first = TestResources.channelWorker.getMembers().stream()
                    .filter(x -> x.getId().equals(TestResources.testerJDA.getSelfUser().getId())).findFirst();
        }
        Assert.assertTrue(first.isPresent());

        if (isLeaderboard) {
            header = first.get().getGuild().getName();

        } else {
            header = first.get().getEffectiveName();
        }
        if (artistThumbnail == null) {
            artistThumbnail = isLeaderboard ? serverThumbnail : first.get().getUser().getAvatarUrl();
        }
        Pattern footerRegex = Pattern
                .compile("(" + header + " has (?:(\\d+) (?:global )?((?:album )?crown(s)?!|registered user(s)?|unique artist(s)?|)|stolen (\\d+) crown(?:s)? {2})|(.*) has stolen \\d+ crowns)!");

        Pattern titlePattern = Pattern.compile(titleRegex.replaceAll("\\$\\{header}", header));

        new EmbedTesterBuilder(command)
                .footernPattern(footerRegex)
                .titlePattern(titlePattern)
                .descriptionPattern(descriptionRegex)
                .descriptionMatcher(matcherDescription)
                .noEmbbed(NoEmbededPattern)
                .timeout(45)
                .hasThumbnail(true)
                .thumbnail(artistThumbnail)
                .build().GeneralFunction();


    }


    public static void testLeaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, boolean hasPing, Pattern NoEmbededPattern) {
        testLeaderboardEmbed(command, descriptionRegex, titleRegex, isLeaderboard, hasPing, null, NoEmbededPattern);
    }

    public static void testLeaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, Pattern NoEmbededPattern) {
        testLeaderboardEmbed(command, descriptionRegex, titleRegex, isLeaderboard, false, null, NoEmbededPattern);
    }


    public static void testNonleaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, Pattern NoEmbededPattern) {
        testLeaderboardEmbed(command, descriptionRegex, titleRegex, isLeaderboard, false, null, NoEmbededPattern);
    }


    @Before
    public void setUp() {
        testerJDAThumbnail = TestResources.testerJDA.getSelfUser().getAvatarUrl();
        ogJDAThumbnail = TestResources.ogJDA.getSelfUser().getAvatarUrl();
        serverThumbnail = TestResources.channelWorker.getGuild().getIconUrl();
    }


}
