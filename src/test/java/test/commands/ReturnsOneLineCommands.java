package test.commands;

import dao.entities.TimeFrameEnum;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.OneLineUtils;
import test.commands.utils.TestResources;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ReturnsOneLineCommands {
    @ClassRule
    public static final TestRule res = TestResources.INSTANCE;


    @Test
    public void testParsers() {
        NullReturnParsersTest.onlyUsernameParser("!scrobbled");
        NullReturnParsersTest.usernameAndNpQueryParser("!yt");
        NullReturnParsersTest.timerFrameParser("!minutes");
        NullReturnParsersTest.npParser("!nps");
        NullReturnParsersTest.prefixParser("!prefix");

    }

    @Test
    public void scrobbledTest() {
        Pattern pattern = Pattern.compile("(.*?)(?= has scrobbled) has scrobbled (\\d+) different artists");
        Predicate<Matcher> function = matcher ->
                matcher.group(1).equals(TestResources.testerJdaUsername) && (Integer.parseInt(matcher.group(2)) >= 0);

        OneLineUtils.testCommands("!scrobbled", pattern, function);
    }

    @Test
    public void minutesTest() {
        Pattern pattern = Pattern
                .compile("(.*?)(?= played ) played (\\d+) minutes? of music, \\((\\d+):(\\d+) hours?\\), listening to (\\d+) different tracks? in the last (.*)");
        Predicate<Matcher> function = matcher ->
        {
            int totalMinutes = Integer.parseInt(matcher.group(2));
            int totalHours = Integer.parseInt(matcher.group(3));
            int partialMinutes = Integer.parseInt(matcher.group(4));
            int differentTracks = Integer.parseInt(matcher.group(5));

            return totalMinutes > 0 &&
                    totalHours >= 0 &&
                    partialMinutes >= 0 &&
                    differentTracks > 0 &&
                    totalMinutes == totalHours * 60 + partialMinutes &&
                    matcher.group(1).equals(TestResources.testerJdaUsername) &&
                    TimeFrameEnum.WEEK.toString().equalsIgnoreCase(matcher.group(6));
        };
        OneLineUtils.testCommands("!minutes", pattern, function);
    }

    @Test
    public void minutesErrorMessageTest() {
        Pattern pattern = Pattern
                .compile("Only \\[w]eek,\\[m]onth and \\[q]uarter are supported at the moment , sorry :'\\(");
        OneLineUtils.testCommands("!minutes a", pattern, null);
    }

    @Test
    public void youtubeSearch() {
        Pattern youtubePattern = Pattern
                .compile("(?:https?://)?(?:www\\.)?youtu\\.?be(?:\\.com)?/?.*(?:watch|embed)?(?:.*v=|v/|/)([\\w-_]{11})(?:.*)?$");
        OneLineUtils.testCommands("!yt joy division", youtubePattern, null);
        OneLineUtils.testCommands("!yt ", youtubePattern, null);

    }

    @Test
    public void youtubeSearchFailure() {
        Pattern youtubePattern = Pattern
                .compile("Coudn't find \"([^\"]*)\" on youtube");
        Predicate<Matcher> function = matcher -> matcher.group(1).equals("ausjdjdmxcjasdi,zxcia".repeat(2));
        OneLineUtils.testCommands("!yt " + "ausjdjdmxcjasdi,zxcia".repeat(2), youtubePattern, function);
    }

    @Test
    public void spotifyNpSearch() {
        Pattern spotify = Pattern
                .compile(
                        "(^(https://open.spotify.com/(album|artist|track|playlist)/|spotify:(album|artist|track|playlist):)([a-zA-Z0-9]{22})(?:\\?.*)?$|" +
                                "(Was not able to find (.*) on spotify))");
        OneLineUtils.testCommands("!nps", spotify);
    }


    @Test
    public void prefixSuccesfull() {

        String s = "(.*)The prefix must be one of the following:(.*)";
        OneLineUtils.testCommands("!prefix €E€!", Pattern.compile(s, Pattern.DOTALL), null);

        Pattern pattern = Pattern
                .compile("(.) is the new server prefix");

        Predicate<Matcher> function = matcher -> matcher.group(1).equals("!");
        OneLineUtils.testCommands("!prefix !", pattern, function);
    }

    @Test
    public void albumPlays() {
        Pattern pattern = Pattern.compile("(.*?)(?= has listened) has listened (.*?) (\\d+) (times?)");
        Function<String, Predicate<Matcher>> parentFunction = string -> matcher1 -> {
            int plays = Integer.parseInt(matcher1.group(3));
            return matcher1.group(1).equals(TestResources.testerJdaUsername) &&
                    matcher1.group(2).equalsIgnoreCase(string) &&
                    plays >= 0 && matcher1.group(4).equals(plays == 1 ? "time" : "times");

        };
        OneLineUtils.testCommands("!album ", pattern, null);
        OneLineUtils.testCommands("!album " + TestResources.testerJDA.getSelfUser().getAsMention(), pattern, null);

        OneLineUtils.testCommands("!album blackpink - square up ", pattern, parentFunction.apply("square up"));
        OneLineUtils.testCommands("!album my bloody valentine - loveless ", pattern, parentFunction.apply("loveless"));


    }

    @Test
    public void ParserErrorsAlbumPlays() {
        Pattern compile = Pattern.compile("(.*)You need to use - to separate artist and album!", Pattern.DOTALL);
        OneLineUtils.testCommands("!album my bloody valentine  loveless ", compile, null);

    }
}
