package main.Commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public abstract class MyCommand extends ListenerAdapter {

	public abstract void onCommand(MessageReceivedEvent e, String[] args);

	public abstract List<String> getAliases();

	public abstract String getDescription();

	public abstract String getName();

	public abstract List<String> getUsageInstructions();

	public abstract String[] parse(MessageReceivedEvent e) throws ParseException;


	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() && !respondToBots())
			return;
		if (containsCommand(e.getMessage()))
			onCommand(e, commandArgs(e.getMessage()));
	}

	protected boolean containsCommand(Message message) {
		return getAliases().contains(commandArgs(message)[0]);
	}

	protected String[] commandArgs(Message message) {
		return commandArgs(message.getContentDisplay());
	}

	protected String[] commandArgs(String string) {
		return string.split(" ");
	}

	protected Message sendMessage(MessageReceivedEvent e, Message message) {
		if (e.isFromType(ChannelType.PRIVATE))
			return e.getPrivateChannel().sendMessage(message).complete();
		else
			return e.getTextChannel().sendMessage(message).complete();
	}

	protected Message sendMessage(MessageReceivedEvent e, String message) {
		return sendMessage(e, new MessageBuilder().append(message).build());
	}

	protected boolean respondToBots() {
		return false;
	}

	public void onLastFMError(MessageReceivedEvent event) {
		MessageBuilder messageBuilder = new MessageBuilder();
		messageBuilder.setContent("An error happened with the Last.fm API, Try again later");
		messageBuilder.sendTo(event.getChannel()).queue();

	}

	public String[] getSubMessage(Message message) {
		String[] parts = message.getContentRaw().substring(1).split("\\s+");

		return Arrays.copyOfRange(parts, 1, parts.length);

	}


}
