package main.Parsers;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class Parser {
	final Map<Integer, String> errorMessages = new HashMap<>(10);

	Parser() {
		setUpErrorMessages();
	}

	public abstract String[] parse(MessageReceivedEvent e);

	protected abstract void setUpErrorMessages();

	public String getErrorMessage(int code) {
		return errorMessages.get(code);
	}


	String[] containsOptional(String optional, String[] subMessage) {
		return Arrays.stream(subMessage).filter(s -> !s.equals("--" + optional)).toArray(String[]::new);
	}

	boolean isValidURL(String urlString) {
		try {
			URL url = new URL(urlString);
			url.toURI();
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	String artistMultipleWords(String[] message) {
		String artist;
		if (message.length > 1) {
			StringBuilder a = new StringBuilder();
			for (String s : message) {
				a.append(s).append(" ");
			}
			artist = a.toString().trim();
		} else {
			artist = message[0];
		}
		return artist;
	}

	String[] getSubMessage(String string) {
		String[] parts = string.substring(1).split("\\s+");
		return Arrays.copyOfRange(parts, 1, parts.length);

	}

	String[] getSubMessage(Message message) {
		return getSubMessage(message.getContentRaw());

	}

	public Message sendError(String message, MessageReceivedEvent e) {
		String errorBase = "Error on " + e.getAuthor().getName() + "'s request:\n";
		return sendMessage(new MessageBuilder().append(errorBase).append(message).build(), e);
	}

	public Message sendMessage(String message, MessageReceivedEvent e) {
		return sendMessage(new MessageBuilder().append(message).build(), e);
	}

	private Message sendMessage(Message message, MessageReceivedEvent e) {
		if (e.isFromType(ChannelType.PRIVATE))
			return e.getPrivateChannel().sendMessage(message).complete();
		else
			return e.getTextChannel().sendMessage(message).complete();
	}
}
