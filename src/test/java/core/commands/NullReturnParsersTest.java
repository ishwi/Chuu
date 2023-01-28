package core.commands;

import core.commands.abstracts.MyCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.jupiter.api.extension.ExtendWith;
import test.commands.parsers.TestAssertion;
import test.commands.utils.TestResources;
import test.runner.AssertionRunner;

import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static test.commands.utils.TestResources.channelWorker;
import static test.commands.utils.TestResources.testerJDA;

@ExtendWith(TestResources.class)
public class NullReturnParsersTest {
    public static void artistSongParser(MyCommand<?> command, String args) {
        artistAlbumParser(command, args);
    }

    //Two pings means it fails
    public static void artistAlbumParser(MyCommand<?> command, String args) {
        AssertionRunner.fromCommand(command, args)
                .assertion(List.of(
                        TestAssertion.typing(),
                        TestAssertion.text(txt -> txt.error(charSequence -> {
                            assertThat(charSequence).isEqualTo("Only one user pls");
                        }))
                ));
    }

    private static void assertEqualsErrorMessage(String expected, Message message) {
        String reallyExpected = "Error on " + testerJDA.getSelfUser().getName() + "'s request:\n" + expected;
        assertThat(message.getContentStripped()).isEqualTo(reallyExpected);
    }

    public static void artistTimeFrameParser(MyCommand<?> myCommand, String command) {
        artistAlbumParser(myCommand, command);
    }

    public static void artistUrlParser(MyCommand<?> myCommand, String args) {
        noWordsFailure(args, "You need to specify the artist and the url !!");
        moreThanXWordsFailure(myCommand, args, "You didnt specify a valid URL", 1);
    }

    //No input means it fails
    private static void noWordsFailure(String command, String expected) {
        long id = channelWorker.sendMessage(command + " ").complete().getIdLong();
        await().atMost(45, TimeUnit.SECONDS).until(() ->
        {
            MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
            return complete.getRetrievedHistory().size() == 1;
        });
        Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
        assertEqualsErrorMessage(expected, message);
    }

    private static void moreThanXWordsFailure(MyCommand<?> myCommand, String command, String expected, int numberOfWords) {
        StringBuilder sb = new StringBuilder();
        assert numberOfWords >= 0;
        for (int i = 1; i <= numberOfWords; i++) {
            sb.append(" test").append(i);
        }
        AssertionRunner.fromCommand(myCommand, command + sb).assertion(List.of(
                TestAssertion.typing(),
                TestAssertion.text(sendText -> sendText.error(x -> assertThat(x).isEqualTo(expected)))
        ));
    }

    //Fails With three words
    public static void chartFromYearParser(MyCommand<?> command, String args) {
        moreThanXWordsFailure(command, args, "You Introduced too many words", 4);
        futureYear(command, args);
    }

    private static void futureYear(MyCommand<?> myCommand, String command) {
        Year year = Year.now().plus(2, ChronoUnit.YEARS);

        AssertionRunner.fromCommand(myCommand, command + " " + year).assertion(List.of(
                TestAssertion.typing(),
                TestAssertion.error(charSequence -> {
                    assertThat(charSequence).isEqualTo("YEAR must be current year or lower");
                })
        ));

    }

    //Fails With four words
    public static void chartParser(MyCommand<?> myCommand, String command) {
        moreThanXWordsFailure(myCommand, command, "You Introduced too many words", 4);
    }

    public static void npParser(String command) {
        daoParser(command);
    }

    //Mentions someone not registered
    private static void daoParser(String command) {
		/*long id = channelWorker.sendMessage(command + " " + ogJDA.getUserById(developerId).getAsMention()).complete()
				.getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		Member memberById = channelWorker.getGuild().getMemberById(developerId);
		assert memberById != null;
		String effectiveName = memberById.getEffectiveName();
		String expeceted = effectiveName +
				" has not set their last.fm account\n" +
				"To link to the bot you must have a last.fm account and then do:\n" +
				" !set your_last_fm_account";
		assertEqualsErrorMessage(expeceted, message);*/
    }

    public static void oneWordParser(String command) {
        noWordsFailure(command, "You need to introduce a word!");
    }

    public static void onlyUsernameParser(String command) {
        daoParser(command);
    }

    public static void prefixParser(String command) {
        noWordsFailure(command, "Pls only intruduce the prefix you want the bot to use");

    }

    public static void randomAlbumParser(MyCommand<?> myCommand, String command) {
        moreThanXWordsFailure(myCommand, command, "Invalid url, only accepts spotify uri or url, yt url, deezer's url and soundcloud's url", 1);
        moreThanXWordsFailure(myCommand, command, "Only one word was expected", 2);
    }

    public static void timerFrameParser(String command) {
        daoParser(command);
    }

    public static void topParser(String command) {
        daoParser(command);
    }

    public static void twoUsersParser(String command) {
        noWordsFailure(command, "Need at least one username");
    }

    public static void urlParser(MyCommand<?> myCommand, String command) {
        moreThanXWordsFailure(myCommand, command, "You need to give only a url or an attachment", 3);
    }

    public static void usernameAndNpQueryParser(MyCommand<?> myCommand, String command) {
        artistParser(myCommand, command);
    }

    public static void artistParser(MyCommand<?> myCommand, String command) {
        artistAlbumParser(myCommand, command);
    }

    public static void scoreOnAlbumError(String command) {
        long id = channelWorker.sendMessage(command + " RED VELVET - Perfect Velvet - The 2nd Album ").complete()
                .getIdLong();
        await().atMost(45, TimeUnit.SECONDS).until(() ->
        {
            MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
            return complete.getRetrievedHistory().size() == 1;
        });
        Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
        assertEqualsErrorMessage("You need to add the escape character \"\\\\\" in the \"-\" that appear on the album or artist.\n" +
                                 " \tFor example: Artist - Alb\\\\-um", message);


    }

    public static void setParser(String command) {
        noWordsFailure(command, "You need to introduce only a valid last.fm account!");
    }

}
