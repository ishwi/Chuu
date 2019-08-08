package main.Commands;

import main.Parsers.Parser;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class MyCommand extends ListenerAdapter {
	final String PREFIX = "!";
	boolean respondInPrivate = true;
	Parser parser;

	abstract void onCommand(MessageReceivedEvent e, String[] args);

	abstract String getDescription();

	abstract String getName();

	public String getUsageInstructions() {
		return parser.getUsage(getAliases().get(0));
	}

	abstract List<String> getAliases();

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (!e.getMessage().getContentRaw().startsWith(PREFIX) || (e.getAuthor().isBot() && !respondToBots()))
			return;

		if (containsCommand(e.getMessage())) {
			e.getChannel().sendTyping().queue();
			System.out.println("We received a message from " +
					e.getAuthor().getName() + "; " + e.getMessage().getContentDisplay());
			if (!e.getChannelType().isGuild() && !respondInPrivate) {
				sendMessage(e, "This command only works in a server");
				return;
			}
			measureTime(e);


		}
	}

	private void measureTime(MessageReceivedEvent e) {
		long startTime = System.currentTimeMillis();
		onCommand(e, commandArgs(e.getMessage()));
		long endTime = System.currentTimeMillis();
		long timeElapsed = endTime - startTime;
		System.out.println("Execution time in milliseconds " + getName() + " : " + timeElapsed);
		System.out.println();
	}

	boolean containsCommand(Message message) {
		return getAliases().contains(commandArgs(message)[0]);
	}

	String[] commandArgs(Message message) {
		return commandArgs(message.getContentDisplay());
	}

	private String[] commandArgs(String string) {
		return string.split(" ");
	}

	@SuppressWarnings("SameReturnValue")
	boolean respondToBots() {
		return false;
	}

	String[] getSubMessage(Message message) {
		String[] parts = message.getContentRaw().substring(1).split("\\s+");

		return Arrays.copyOfRange(parts, 1, parts.length);

	}

	void sendImage(BufferedImage image, MessageReceivedEvent e) {
		sendImage(image, e, false);
	}

	void sendImage(BufferedImage image, MessageReceivedEvent e, boolean makeSmaller) {
		//MessageBuilder messageBuilder = new MessageBuilder();
		if (image == null) {
			sendMessage(e, "Ish Pc Bad");
			return;
		}
		ByteArrayOutputStream b = new ByteArrayOutputStream();

		try {
			String format = "png";
			if (makeSmaller)
				format = "jpg";
			ImageIO.write(image, format, b);

			byte[] img = b.toByteArray();
			if (img.length < 8388608)
				e.getChannel().sendFile(img, "cat." + format).queue();
				//messageBuilder.sendTo(e.getChannel()).addFile(img, "cat.png").queue();
			else
				e.getChannel().sendMessage("Boot too big").queue();
			//messageBuilder.setContent("Boot to big").sendTo(e.getChannel()).queue();

		} catch (IOException ex) {
			sendMessage(e, "Ish Pc Bad");
			ex.printStackTrace();
		}


	}

	Message sendMessage(MessageReceivedEvent e, String message) {
		return sendMessage(e, new MessageBuilder().append(message).build());
	}

	private Message sendMessage(MessageReceivedEvent e, Message message) {
		if (e.isFromType(ChannelType.PRIVATE))
			return e.getPrivateChannel().sendMessage(message).complete();
		else
			return e.getTextChannel().sendMessage(message).complete();
	}

	String getUserString(Long discordId, MessageReceivedEvent e, String replacement) {

		Member member = e.getGuild().getMemberById(discordId);
		return member == null ? replacement : member.getEffectiveName();
	}


}
