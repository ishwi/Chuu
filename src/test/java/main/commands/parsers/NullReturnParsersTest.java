package main.commands.parsers;

import main.commands.utils.TestResources;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.Assert;
import org.junit.ClassRule;

import java.util.concurrent.TimeUnit;

import static main.commands.utils.TestResources.*;
import static org.awaitility.Awaitility.await;

public class NullReturnParsersTest {
	@ClassRule
	public static final TestResources res = new TestResources();

	public static void artistSongParser(String command) {
		artistAlbumParser(command);
	}

	//Two pings means it fails
	public static void artistAlbumParser(String command) {
		long id = channelWorker
				.sendMessage(command + " " + ogJDA.getSelfUser().getAsMention() + " " + testerJDA.getSelfUser()
						.getAsMention()).complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);

		assertEqualsErrorMessage("Only one user pls", message);
	}

	private static void assertEqualsErrorMessage(String expected, Message message) {
		String reallyExpected = "Error on " + testerJDA.getSelfUser().getName() + "'s request:\n" + expected;
		Assert.assertEquals(reallyExpected, message.getContentStripped());
	}

	public static void artistTimeFrameParser(String command) {
		artistAlbumParser(command);
	}

	public static void artistUrlParser(String command) {
		noWordsFailure(command, "You need to specify the artist and the url !!");
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

	//Fails With three words
	public static void chartFromYearParser(String command) {
		moreThanXWordsFailure(command, "You Introduced too many words", 3);
	}

	private static void moreThanXWordsFailure(String command, String expected, int numberOfWords) {
		StringBuilder sb = new StringBuilder();
		assert numberOfWords >= 0;
		for (int i = 1; i <= numberOfWords; i++) {
			sb.append(" test").append(i);
		}
		long id = channelWorker.sendMessage(command + sb.toString()).complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		assertEqualsErrorMessage(expected, message);
	}

	//Fails With four words
	public static void chartParser(String command) {
		moreThanXWordsFailure(command, "You Introduced too many words", 4);
	}

	public static void npParser(String command) {
		daoParser(command);
	}

	//Mentions someone not registered
	private static void daoParser(String command) {
		long id = channelWorker.sendMessage(command + " " + ogJDA.getUserById(developerId).getAsMention()).complete()
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
		assertEqualsErrorMessage(expeceted, message);
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

	public static void randomAlbumParser(String command) {
		moreThanXWordsFailure(command, "Invalid url, only accepts spotify uri or url, yt url, deezer's url and soundcloud's url", 1);
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

	public static void urlParser(String command) {
		moreThanXWordsFailure(command, "You need to give only a url or an attachment", 3);
	}

	public static void usernameAndNpQueryParser(String command) {
		artistParser(command);
	}

	public static void artistParser(String command) {
		artistAlbumParser(command);
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


}
