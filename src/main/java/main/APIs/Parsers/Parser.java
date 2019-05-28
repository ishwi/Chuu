package main.APIs.Parsers;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class Parser {
	public final String errorBase;
	public Map<Integer, String> errorMessages = new HashMap<>(10);
	public MessageReceivedEvent e;

	public Parser(MessageReceivedEvent e) {
		this.e = e;
		this.errorBase = "Error on " + e.getAuthor().getName() + "'s request:\n";
		setUpErrorMessages();
	}

	public abstract String[] parse();

	public abstract void setUpErrorMessages();

	public String getErrorMessage(int code) {
		return errorMessages.get(code);
	}


	public String[] containsOptional(String optional, String[] subMessage) {
		return Arrays.stream(subMessage).filter(s -> !s.equals("--" + optional)).toArray(String[]::new);
	}

	public boolean isValidURL(String urlString) {
		try {
			URL url = new URL(urlString);
			url.toURI();
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	public String artistMultipleWords(String[] message) {
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


	public String[] getSubMessage(Message message) {
		String[] parts = message.getContentRaw().substring(1).split("\\s+");

		return Arrays.copyOfRange(parts, 1, parts.length);

	}

	public Message sendError(String message) {
		return sendMessage(new MessageBuilder().append(errorBase).append(message).build());
	}

	public Message sendMessage(String message) {
		return sendMessage(new MessageBuilder().append(message).build());
	}

	public Message sendMessage(Message message) {
		if (e.isFromType(ChannelType.PRIVATE))
			return e.getPrivateChannel().sendMessage(message).complete();
		else
			return e.getTextChannel().sendMessage(message).complete();
	}
}
