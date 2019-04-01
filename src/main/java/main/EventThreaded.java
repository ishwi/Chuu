package main;

import DAO.DaoImplementation;
import DAO.Entities.*;
import main.ImageRenderer.NPMaker;
import main.ImageRenderer.imageRenderer;
import main.last.LastFMService;
import main.last.LastFMServiceException;
import main.last.UpdaterThread;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import javax.management.InstanceNotFoundException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

class EventThreaded implements Runnable {

	private final MessageReceivedEvent event;
	private final DaoImplementation impl;
	private final LastFMService lastAccess;
	private final Spotify spotify;

	public EventThreaded(MessageReceivedEvent event, DaoImplementation dao, LastFMService last, Spotify spotify) {
		this.event = event;
		this.impl = dao;
		this.spotify = spotify;
		this.lastAccess = last;
	}

	@Override
	public void run() {

		System.out.println("We received a message from " +
				event.getAuthor().getName() + "; " + event.getMessage().getContentDisplay());
		String message1 = event.getMessage().getContentRaw().substring(1);
		String[] message = message1.split("\\s+");
		String[] subMessage = Arrays.copyOfRange(message, 1, message.length);


		switch (message[0]) {
			case "chart":
				onChartMessageReceived(subMessage, event);
				break;
			case "top":
				onTopMessageReceived(subMessage, event.getAuthor().getIdLong(), event);
				break;

			case "update":
				onUpdate(subMessage, event.getAuthor().getIdLong(), event);
				break;
			case "ping":
				event.getChannel().sendMessage("!pong").queue();
				break;
			case "set":
				onSetterMessageReceived(subMessage, event.getAuthor().getIdLong(), event);
				break;
			case "taste":
				onTaste(subMessage, event.getAuthor().getIdLong(), event);
				break;
			case "whoknowsnp":
				onWhoKnows(subMessage, event.getAuthor().getIdLong(), event);
				break;
			case "whoknows":
				onWhoKnowsArtist(subMessage, event);
				break;
			case "np":
				onNowPlaying(subMessage, event);
				break;
			case "playing":
				onAllPlaying(subMessage, event);
				break;
			case "npspotify":
				onNPSpotify(subMessage, event);
				break;
			default:
				printHelp(event);

		}


	}

	private void onNPSpotify(String[] subMessage, MessageReceivedEvent event) {
		String username;
		MessageBuilder messageBuilder = new MessageBuilder();
		try {
			username = getLastFmUsername1input(subMessage, event.getAuthor().getIdLong(), event);
			NowPlayingArtist nowPlayingArtist = lastAccess.getNowPlayingInfo(username);
			StringBuilder a = new StringBuilder();
			event.getChannel().sendTyping().queue();
			String uri = spotify.searchItems(nowPlayingArtist.getSongName(), nowPlayingArtist.getArtistName(), nowPlayingArtist.getAlbumName());
			messageBuilder.setContent(uri).sendTo(event.getChannel()).queue();
		} catch (InstanceNotFoundException | LastFMServiceException e) {
			e.printStackTrace();
		}

	}

	private void onAllPlaying(String[] subMessage, MessageReceivedEvent event) {
		boolean noFlag = Arrays.stream(subMessage).noneMatch(s -> s.equals("--recent"));
		List<UsersWrapper> list = impl.getAll(event.getGuild().getIdLong());
		MessageBuilder messageBuilder = new MessageBuilder();

		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(randomColor()).setThumbnail(event.getGuild().getIconUrl())
				.setTitle("What is being played now in " + event.getGuild().getName());
		StringBuilder a = new StringBuilder();
		event.getChannel().sendTyping().queue();


		for (UsersWrapper usersWrapper : list) {

			try {
				NowPlayingArtist nowPlayingArtist = lastAccess.getNowPlayingInfo(usersWrapper.getLastFMName());
				if (noFlag) {
					if (!nowPlayingArtist.isNowPlaying()) {
						continue;
					}
				}

				String username = event.getGuild().getMemberById(usersWrapper.getDiscordID()).getEffectiveName();
				a.append("+ ").append("[")
						.append(username).append("](").append("https://www.last.fm/user/").append(usersWrapper.getLastFMName())
						.append("): ")
						.append("**").append(nowPlayingArtist.getSongName()).append("**")
						.append(" - ").append(nowPlayingArtist.getAlbumName()).append(" | ")
						.append(nowPlayingArtist.getArtistName()).append("\n");
			} catch (LastFMServiceException e) {
			}


		}
		embedBuilder.setDescription(a);
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(event.getChannel()).queue();
	}

	private void onNowPlaying(String[] subMessage, MessageReceivedEvent event) {
		try {
			String username = getLastFmUsername1input(subMessage, event.getAuthor().getIdLong(), event);
			NowPlayingArtist nowPlayingArtist = lastAccess.getNowPlayingInfo(username);
			StringBuilder a = new StringBuilder();
			event.getChannel().sendTyping().queue();

			a.append("**[").append(username).append("'s Profile](").append("https://www.last.fm/user/").append(username).append(")**\n\n")
					.append(nowPlayingArtist.isNowPlaying() ? "Current" : "Last")
					.append(":\n")
					.append("**").append(nowPlayingArtist.getSongName()).append("**")
					.append(" - ").append(nowPlayingArtist.getAlbumName()).append(" | ")
					.append(nowPlayingArtist.getArtistName());

			EmbedBuilder embedBuilder = new EmbedBuilder().setColor(randomColor()).setThumbnail(nowPlayingArtist.getUrl())
					.setTitle("Now Playing:")
					.setDescription(a);

			MessageBuilder messageBuilder = new MessageBuilder();
			messageBuilder.setEmbed(embedBuilder.build()).sendTo(event.getChannel()).queue();

		} catch (InstanceNotFoundException e) {
			userNotOnDB(event);
		} catch (LastFMServiceException e) {
			onLastFMError(event);
		}
	}


	private void printHelp(MessageReceivedEvent event) {

		MessageBuilder mes = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		StringBuilder a = new StringBuilder();
		String[] methods = {"set", "update", "chart", "top", "taste", "whoknowsnp", "whoknows", "np", "playing"};
		for (String s : methods) {
			a.append(printCommand(s));
		}
		embedBuilder.setDescription(a);
		embedBuilder.setTitle("List Of commands \n");

		embedBuilder.setColor(Color.LIGHT_GRAY);
		mes.setEmbed(embedBuilder.build()).sendTo(event.getChannel()).queue();
	}

	private void onSetterMessageReceived(String[] message, long id, MessageReceivedEvent event) {
		MessageBuilder mes = new MessageBuilder();
		if ((message.length > 1) || (message.length == 0)) {
			return;
		}
		String lastFmID = message[0];
		long guildID = event.getGuild().getIdLong();
		impl.addData(new LastFMData(lastFmID, id, guildID));

		new Thread(new UpdaterThread(impl, lastAccess)).run();
		mes.setContent(event.getAuthor().getName() + "Has set his last FM name \n Updating his library on the background");
		mes.sendTo(event.getChannel()).queue();
	}

	private void onWhoKnowsArtist(String[] message, MessageReceivedEvent event) {


		MessageBuilder messageBuilder = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		if (message.length == 0) {
			messageBuilder.setContent(printUsage("whoknows")).sendTo(event.getChannel()).queue();
			return;
		}
		boolean flag = false;
		String[] message1 = Arrays.stream(message).filter(s -> !s.equals("--image")).toArray(String[]::new);
		if (message1.length != message.length) {
			message = message1;
			flag = true;
		}
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

		WrapperReturnNowPlaying wrapperReturnNowPlaying = impl.whoKnows(artist, event.getGuild().getIdLong());
		try {

			if (!flag) {
				StringBuilder builder = new StringBuilder();
				int counter = 1;
				for (ReturnNowPlaying returnNowPlaying : wrapperReturnNowPlaying.getReturnNowPlayings()) {
					String userName = event.getGuild().getMemberById(returnNowPlaying.getDiscordId()).getEffectiveName();
					builder.append(counter++)
							.append(". ")
							.append("[").append(userName).append("]")
							.append("(https://www.last.fm/user/").append(returnNowPlaying.getLastFMId()).append(") - ")
							.append(returnNowPlaying.getPlaynumber()).append(" plays\n");
				}

				embedBuilder.setTitle("Who knows " + wrapperReturnNowPlaying.getArtist() + " in " + event.getGuild().getName() + "?").
						setThumbnail(wrapperReturnNowPlaying.getUrl()).setDescription(builder)
						.setColor(randomColor());
				//.setFooter("Command invoked by " + event.getMember().getUser().getDiscriminator() + " · " + LocalDateTime.now().format(DateTimeFormatter.ISO_WEEK_DATE).toString(), );
				messageBuilder.setEmbed(embedBuilder.build()).sendTo(event.getChannel()).queue();
				return;
			}
			BufferedImage image = NPMaker.generateNP(wrapperReturnNowPlaying, event.getGuild().getName());
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", b);
			byte[] img = b.toByteArray();
			if (img.length < 8388608)
				messageBuilder.sendTo(event.getChannel()).addFile(img, "cat.png").queue();


		} catch (IOException | IllegalArgumentException e2) {
			messageBuilder.setContent("No nibba listens to " + message[0]).sendTo(event.getChannel()).queue();


		}
	}

	private void onWhoKnows(String[] message, long id, MessageReceivedEvent event) {


		try {

			event.getChannel().sendTyping().queue();
			String username = getLastFmUsername1input(message, id, event);
			NowPlayingArtist nowPlayingArtist = lastAccess.getNowPlayingInfo(username);
			onWhoKnowsArtist(new String[]{nowPlayingArtist.getArtistName()}, event);

		} catch (LastFMServiceException e) {
			onLastFMError(event);
		} catch (InstanceNotFoundException e) {
			userNotOnDB(event);
		}

	}

	private void onLastFMError(MessageReceivedEvent event) {
		MessageBuilder messageBuilder = new MessageBuilder();
		messageBuilder.setContent("An error happened with the Last.fm API, Try again later");
		messageBuilder.sendTo(event.getChannel()).queue();

	}

	private Color randomColor() {
		Random rand = new Random();
		double r = rand.nextFloat() / 2f + 0.5;
		double g = rand.nextFloat() / 2f + 0.5;
		double b = rand.nextFloat() / 2f + 0.5;
		return new Color((float) r, (float) g, (float) b);
	}

	private void onUpdate(String[] message, long id, MessageReceivedEvent event) {
		String username;

		MessageBuilder a = new MessageBuilder();
		a.setContent("Starting to update your profile");
		a.sendTo(event.getChannel()).queue();

		event.getChannel().sendTyping().queue();
		try {
			username = getLastFmUsername1input(message, id, event);
			LinkedList<ArtistData> list = lastAccess.getLibrary(username);
			impl.addData(list, username);
			a.setContent("Sucessfully updated" + username + " info !").sendTo(event.getChannel()).queue();


		} catch (InstanceNotFoundException e) {
			userNotOnDB(event);


		} catch (LastFMServiceException e) {
			onLastFMError(event);
		}

	}

	private void onTaste(String[] message, long id, MessageReceivedEvent event) {
		//message 0
		//message 1 *optional
		MessageBuilder messageBuilder = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		if (message.length == 0) {
			messageBuilder.setContent(printUsage("taste"));
			messageBuilder.sendTo(event.getChannel()).queue();
			return;
		}
		event.getChannel().sendTyping().queue();
		String[] userList = {"", ""};
		if (message.length == 1) {
			userList[1] = message[0];
			try {
				userList[0] = impl.findShow(id).getName();
			} catch (InstanceNotFoundException e) {
				userNotOnDB(event);
				return;
			}
		} else {
			userList[0] = message[0];
			userList[1] = message[1];
		}

		// Si userList contains @ -> user
		try {
			java.util.List<User> list = event.getMessage().getMentionedUsers();
			java.util.List<String> lastfMNames = Arrays.stream(userList)
					.map(s -> lambda(s, list))
					.collect(Collectors.toList());
			lastfMNames.forEach(System.out::println);

			ResultWrapper resultWrapper = impl.getSimilarities(lastfMNames);
			System.out.println("resultWrapper = " + resultWrapper.getRows());
			java.util.List<String> users = new ArrayList<>();
			users.add(resultWrapper.getResultList().get(0).getUserA());
			users.add(resultWrapper.getResultList().get(0).getUserB());
			java.util.List<UserInfo> userInfoLiust = lastAccess.getUserInfo(users);
			BufferedImage image = imageRenderer.generateTasteImage(resultWrapper, userInfoLiust);

			ByteArrayOutputStream b = new ByteArrayOutputStream();
			try {
				ImageIO.write(image, "jpg", b);
				byte[] img = b.toByteArray();
				if (img.length < 8388608) {
					messageBuilder.sendTo(event.getChannel()).addFile(img, "cat.png").queue();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (RuntimeException | InstanceNotFoundException e) {
			userNotOnDB(event);

		} catch (LastFMServiceException e) {
			onLastFMError(event);
		}

	}

	private String printUsage(String command) {
		return "Usage for commands: \n\n" + printCommand(command);
	}

	private String printCommand(String command) {
		switch (command) {
			case "taste":
				return "**!taste user1 *user2 ** \n \tIf user2 is missing it gets replaced by Author user\n\n";

			case "update":
				return "**!update lastFmUser**\n\n";

			case "chart":
				return "**!chart *[w,m,t,y,a] *Username SizeXSize** \n" +
						"\tIf timeframe is not specified defaults to Weekly \n" +
						"\tIf username is not specified defaults to authors account \n" +
						"\tIf size is not specified defaults to 5x5 (As big as discord lets\n\n";


			case "top":
				return "**!top username**\n" + "\tIf username is not specified defaults to authors account \n\n";

			case "set":
				return "**!set lastFMUser**\n\n";
			case "whoknowsnp":
				return "**!whoknowsnp *LastFmUser** \n" +
						"\t If useranme is not specified defaults to authors account\n\n";
			case "whoknows":
				return "**!whoknows artist** \n\n";
			case "np":
				return "**!np *username **\n" +
						"\t If useranme is not specified defaults to authors account\n" +
						"\t --image for image\n\n";
			case "playing":
				return "**!playing ** \n" +
						"--recent for last track\n\n ";
			default:
				return "Something weird Happened";


		}
	}

	private void userNotOnDB(MessageReceivedEvent event) {

		System.out.println("Problemo");
		event.getChannel().sendMessage("User doesnt have an account set").queue();
	}


	private User findUSername(String name, java.util.List<User> userList) {
		Optional<User> match = userList.stream().
				filter(user -> {
					long a = Long.valueOf(name.substring(3, name.indexOf(">")));
					return (user.getIdLong() == a);
				})
				.findFirst();
		return match.orElse(null);
	}

	private String lambda(String s, java.util.List<User> list) {
		if (s.startsWith("<@")) {
			User result = this.findUSername(s, list);
			if (result != null) {
				try {
					return impl.findShow(result.getIdLong()).getName();
				} catch (InstanceNotFoundException e) {
					throw new RuntimeException();
				}
			}
		}
		return s;
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
			java.util.List<User> list;
			list = event.getMessage().getMentionedUsers();
			username = discordName;
			if (!list.isEmpty()) {
				LastFMData data;
				try {
					data = this.impl.findShow((list.get(0).getIdLong()));
				} catch (InstanceNotFoundException e) {
					userNotOnDB(event);
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
			try {
				username = this.impl.findShow(id).getName();
			} catch (InstanceNotFoundException e) {
				userNotOnDB(event);
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
		try {
			byte[] file = this.lastAccess.getUserList(username, time, x, y);
			if (file.length < 8388608) {
				channel.sendFile(file, "cat.png", mes.build()).queue();
				return;
			}
			channel.sendMessage("boot to big").queue();


			String thisMoment = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
					.withZone(ZoneOffset.UTC)
					.format(Instant.now());

			String path = "D:\\Games\\" + thisMoment + ".png";
			try (FileOutputStream fos = new FileOutputStream(path)) {
				fos.write(file);
				//fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
			} catch (IOException e) {
				userNotOnDB(event);
			}


		} catch (LastFMServiceException e) {
			onLastFMError(event);
		}
		// Max Discord File length

	}

	private void onTopMessageReceived(String[] message, long id, MessageReceivedEvent event) {
		MessageBuilder mes = new MessageBuilder();
		EmbedBuilder embed = new EmbedBuilder();

		try {
			String username = getLastFmUsername1input(message, id, event);
			event.getChannel().sendTyping().queue();
			embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
					.setDescription("Most Listened Artists");
			mes.setEmbed(embed.build());
			event.getChannel().sendFile(this.lastAccess.getUserList(username, "overall", 5, 5), "cat.png", mes.build()).queue();
		} catch (InstanceNotFoundException e) {
			userNotOnDB(event);
		} catch (LastFMServiceException e) {
			onLastFMError(event);
		}
	}

	private String getLastFmUsername1input(String[] message, long id, MessageReceivedEvent event) throws InstanceNotFoundException {
		String username;
		if ((message.length > 1) || (message.length == 0)) {
			username = this.impl.findShow(id).getName();
		} else {
			//Caso con @ y sin @
			List<User> list = event.getMessage().getMentionedUsers();
			username = message[0];
			if (!list.isEmpty()) {
				LastFMData data = this.impl.findShow((list.get(0).getIdLong()));
				username = data.getName();
			}
			if (username.startsWith("@")) {
				event.getChannel().sendMessage("Trolled xD").queue();
			}
		}
		return username;
	}


}



