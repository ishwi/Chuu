package main;

import DAO.LastFMData;
import main.last.ConcurrentLastFM;
import main.last.LastFMService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class ListenerLauncher extends ListenerAdapter {
	private LastFMService lastAccess;
	private DaoImplementation impl;

	public ListenerLauncher() {

		this.lastAccess = new ConcurrentLastFM();
		this.impl = new DaoImplementation();

	}


	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String returnText;
		long startTime;

		if (event.getAuthor().isBot() || !event.getMessage().getContentRaw().startsWith("!")) {
			return;
		}

		startTime = System.currentTimeMillis();

		System.out.println("We received a message from " +
				event.getAuthor().getName() + "; " + event.getMessage().getContentDisplay());

		String[] message = event.getMessage().getContentRaw().substring(1).split("\\s+");
		String[] subMessage = Arrays.copyOfRange(message, 1, message.length);


		switch (message[0]) {
			case "chart":
				onChartMessageReceived(subMessage, event);
				break;
			case "top":
				onTopMessageReceived(subMessage, event.getChannel());
				break;

			case "taste":
				onTaste(subMessage, event.getAuthor().getIdLong(), event);
				break;
			case "ping":
				returnText = "!pong";
				event.getChannel().sendMessage(returnText).queue();
				break;
			case "setLastFm":
				onSetterMessageReceived(subMessage, event.getAuthor().getIdLong());
		}


		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println(estimatedTime);
		System.out.println(TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.MILLISECONDS));


	}

	private void onSetterMessageReceived(String[] message, long id) {
		if ((message.length > 1) || (message.length == 0)) {
			return;
		}
		String lastFmID = message[0];
		impl.addData(new LastFMData(lastFmID, id));


	}

	private void onTaste(String[] message, long id, MessageReceivedEvent event) {
		boolean flag = false;
		String username;

		if ((message.length > 1) || (message.length == 0)) {
			//recuperar de db
			username = this.impl.findShow(id).getName();
		} else {
			//Caso con @ y sin @
			List<User> list = event.getMessage().getMentionedUsers();
			username = message[0];
			if (!list.isEmpty()) {
				LastFMData data = this.impl.findShow((list.get(0).getIdLong()));
				if (data == null) {
					System.out.println("Problemo");
					event.getChannel().sendMessage("Userd doesnt have an account set").queue();
					return;
				}
				username = data.getName();
			}
			if (username.startsWith("@")) {
				event.getChannel().sendMessage("Trolled xD").queue();
				return;
			}
		}
		Map<String, Integer> map = lastAccess.getSimiliraties(username);
		impl.addData(map, username);

	}

	private void onChartMessageReceived(String[] message, MessageReceivedEvent event) {
		String time = "7day";
		MessageChannel channel = event.getChannel();
		MessageBuilder mes = new MessageBuilder();
		EmbedBuilder embed = new EmbedBuilder();

//
//        1     !command

//        2     timeFrame 1 char
//        3     Username whatever
//        4     Size    somethingXsomething
		String timeFrame = null;
		boolean isTime = false;
		boolean isName = false;

		String discordName = null;
		int x = 5;
		int y = 5;

		String pattern = "\\d+[xX]\\d+";

		for (String word : message) {
			if (word.length() == 1) {
				timeFrame = word;
				isTime = true;
				continue;
			}
			if (word.matches(pattern)) {
				String[] dim = word.split("[xX]");
				x = Integer.valueOf(dim[0]);
				y = Integer.valueOf(dim[1]);

				continue;
			}
			if (!isName) {
				isName = true;
				discordName = word;
			}
		}


		String username;
		if (isName) {
			List<User> list;
			list = event.getMessage().getMentionedUsers();
			username = discordName;
			if (!list.isEmpty()) {
				LastFMData data = this.impl.findShow((list.get(0).getIdLong()));
				if (data == null) {
					System.out.println("Problemo");
					channel.sendMessage("Userd doesnt have an account set").queue();
					return;
				}
				username = data.getName();
			}
			if (username.startsWith("@")) {
				channel.sendMessage("Trolled xD").queue();
				return;
			}
		} else {
			long id = event.getAuthor().getIdLong();
			username = this.impl.findShow(id).getName();
			if (username == null) {
				System.out.println("error");
				return;
			}
		}


		if (isTime) {
			if (timeFrame.startsWith("y"))
				time = "12month";
			if (timeFrame.startsWith("t"))
				time = "3month";
			if (timeFrame.startsWith("m"))
				time = "1month";
			if (timeFrame.startsWith("a"))
				time = "overall";
		}


		channel.sendTyping().queue();


		embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
				.setDescription("Most Listened Albums in last " + time);
		mes.setEmbed(embed.build());
		if (x * y > 100) {
			channel.sendMessage("Gonna Take a while").queue();
		}

		byte[] file = this.lastAccess.getUserList(username, time, x, y);

		// Max Discord File length
		if (file.length < 8388608) {
			channel.sendFile(file, "cat.png", mes.build()).queue();
			return;
		}

		channel.sendMessage("boot to big").queue();
		try {

			String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
					.withZone(ZoneOffset.UTC)
					.format(Instant.now());

			String path = "D:\\Games\\" + thisMoment + ".png";
			try (FileOutputStream fos = new FileOutputStream(path)) {
				fos.write(file);
				//fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void onTopMessageReceived(String[] message, MessageChannel chan) {
		if (message.length > 0) {


			chan.sendTyping().queue();

			MessageBuilder mes = new MessageBuilder();
			EmbedBuilder embed = new EmbedBuilder();
			embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
					.setDescription("Most Listened Artists");
			mes.setEmbed(embed.build());
			chan.sendFile(this.lastAccess.getUserList(message[0], "overall", 5, 5), "cat.png", mes.build()).queue();
		}


	}


}
