package main.commands;

import main.commands.utils.CommandTest;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static main.commands.utils.TestResources.channelWorker;
import static main.commands.utils.TestResources.ogJDA;
import static org.awaitility.Awaitility.await;

public class HelpCommandTest extends CommandTest {
	private static HelpCommand helpCommand;

	@BeforeClass
	public static void init() {
		Optional<Object> first = ogJDA.getEventManager().getRegisteredListeners().stream()
				.filter(x -> x instanceof HelpCommand).findFirst();
		Assert.assertTrue(first.isPresent());
		helpCommand = (HelpCommand) first.get();
	}

	@Override
	public String giveCommandName() {
		return "!help";
	}

	@Test
	@Override
	public void nullParserReturned() {

	}

	@Test
	public void notExistsMessage() {
		long id = channelWorker.sendMessage("marker").complete().getIdLong();

		//Usually the help message is sent to a private channel but i didnt find a way to get a private message from a marker so i opted to
		//send the message directly to the test channel
		String nonExistingCommand = "NonExistentCommand19219";
		helpCommand.sendPrivate(ogJDA
				.getTextChannelById(channelWorker.getId()), new String[]{"help", nonExistingCommand}, '!');

		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();

			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		String s = "The provided command '**" + nonExistingCommand + "**' does not exist. Use " + "!help to list all commands.";
		Assert.assertEquals(s, message.getContentRaw());

	}

	@Test
	public void BigHelpMessage() {

		long id = channelWorker.sendMessage("marker").complete().getIdLong();

		//Usually the help message is sent to a private channel but i didnt find a way to get a private message from a marker so i opted to
		//send the message directly to the test channel

		helpCommand.sendPrivate(ogJDA.getTextChannelById(channelWorker.getId()), new String[]{}, '!');

		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();

			return complete.getRetrievedHistory().size() == 2;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(1);
		Message message1 = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);

		bigMessageAssert(message, message1);
	}

	private void bigMessageAssert(Message message, Message message1) {
		String[] split = message.getContentStripped().split("\n");
		String[] split1 = message1.getContentStripped().split("\n");

		//All should be commands
		List<String> strings1 = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(split, 11, split.length)));
		List<String> split2 = new ArrayList<>(Arrays.asList(split1));

		strings1.addAll(split2);

		for (String s : strings1) {
			Assert.assertTrue(s.matches("!(\\w+) - .*"));
		}

		long count = ogJDA.getEventManager().getRegisteredListeners().stream().filter(x -> x instanceof MyCommand)
				.count();
		Assert.assertEquals(count, strings1.size());
		Assert.assertEquals(split[0], "The following commands are supported by the bot");
	}

	@Test
	public void smallHelpMessage() {
		long id = channelWorker.sendMessage("marker").complete().getIdLong();
		for (Object registeredListener : ogJDA.getRegisteredListeners()) {
			if (registeredListener instanceof MyCommand) {
				MyCommand command = (MyCommand) registeredListener;
				String alias = command.getAliases().get(0);
				helpCommand
						.sendPrivate(ogJDA.getTextChannelById(channelWorker.getId()), new String[]{"help", alias}, '!');
				long finalId = id;
				await().atMost(45, TimeUnit.SECONDS).until(() ->
				{
					MessageHistory complete = channelWorker.getHistoryAfter(finalId, 20).complete();

					return complete.getRetrievedHistory().size() == 1;
				});
				Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
				id = message.getIdLong();
				String collect = String.join(", ", command.getAliases());
				String expected = "Name: " + command.getName() + "\n"
						+ "Description: " + command.getDescription() + "\n"
						+ "Alliases: " + collect + "\n"
						+ "Usage: !" + command.getUsageInstructions().replaceAll("\\*", "").trim();
				Assert.assertEquals(expected, message.getContentStripped().replaceAll("\\*", "").trim());

			}
		}


	}

	@Test
	public void PrivateMessage() {
		//Cannot send private meessage between bots :(

		Pattern responsePattern = Pattern.compile(
				".+?(?=: Help information was sent as a private message\\.): Help information was sent as a private message\\.");
		long id = channelWorker.sendMessage(COMMAND_ALIAS).complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
		Message message = complete.getRetrievedHistory().get(0);
		Assert.assertTrue(responsePattern.matcher(message.getContentStripped()).matches());
	}

	@Test
	public void ShortHelpViaCommand() {
		//Cannot send private meessage between bots :(
		Optional<ChartCommand> chartCommand = ogJDA.getRegisteredListeners().stream()
				.filter(x -> x instanceof ChartCommand).map(x -> (ChartCommand) x).findFirst();
		Assert.assertTrue(chartCommand.isPresent());
		ChartCommand command = chartCommand.get();
		long id = channelWorker.sendMessage(COMMAND_ALIAS + " chart").complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
		Message message = complete.getRetrievedHistory().get(0);
		String collect = String.join(", ", command.getAliases());

		String expected = "Name: " + command.getName() + "\n"
				+ "Description: " + command.getDescription() + "\n"
				+ "Alliases: " + collect + "\n"
				+ "Usage: !" + command.getUsageInstructions().replaceAll("\\*", "").trim();
		Assert.assertEquals(expected, message.getContentStripped().replaceAll("\\*", "").trim());
	}


}
