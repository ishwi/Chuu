package main.commands;

import dao.DaoImplementation;
import dao.entities.PresenceInfo;
import main.Chuu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FeaturedCommand extends ConcurrentCommand {
	private static final String DEFAULT_USER = "Chuu";
	private static final String DEFAULT_URL = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/180902_%EC%8A%A4%EC%B9%B4%EC%9D%B4%ED%8E%98%EC%8A%A4%ED%8B%B0%EB%B2%8C_%EC%9D%B4%EB%8B%AC%EC%9D%98_%EC%86%8C%EB%85%80_yyxy.jpg/800px-180902_%EC%8A%A4%EC%B9%B4%EC%9D%B4%ED%8E%98%EC%8A%A4%ED%8B%B0%EB%B2%8C_%EC%9D%B4%EB%8B%AC%EC%9D%98_%EC%86%8C%EB%85%80_yyxy.jpg";
	private static final String DEFAULT_ARTIST = "LOOΠΔ";
	private PresenceInfo currentPresence;

	public FeaturedCommand(DaoImplementation dao, ScheduledExecutorService scheduledManager) {
		super(dao);
		currentPresence = new PresenceInfo(DEFAULT_ARTIST, DEFAULT_URL, Long.MAX_VALUE, 1);
		scheduledManager.scheduleAtFixedRate(() -> {
			PresenceInfo randomArtistWithUrl = getDao().getRandomArtistWithUrl();
			Chuu.updatePresence(randomArtistWithUrl.getArtist());
			this.currentPresence = randomArtistWithUrl;
			Chuu.getLogger()
					.info("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE) + "]\t!Updated Presence");
		}, 1, 30, TimeUnit.MINUTES);
	}


	@Override
	List<String> getAliases() {
		return Collections.singletonList("featured");
	}


	@Override
	String getDescription() {
		return "Info About the artist that appears on the bot status";
	}

	@Override
	String getName() {
		return "Featured";
	}

	@Override
	protected void onCommand(MessageReceivedEvent e) {
		String userString = this.getUserGlobalString(currentPresence.getDiscordId(), e, DEFAULT_USER);
		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setColor(CommandUtil.randomColor())
				.setThumbnail(CommandUtil.noImageUrl(currentPresence.getUrl()))
				.setTitle("Chuu's Featured Artist:", CommandUtil.getLastFmArtistUrl(currentPresence.getArtist()))
				.addField("Artist:", currentPresence.getArtist(), false)
				.addField("User:", userString, false)
				.addField("Total Artist Plays:", String.valueOf(currentPresence.getSum()), false);

		MessageBuilder messageBuilder = new MessageBuilder();
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
	}
}
