package main.commands;

import dao.DaoImplementation;
import dao.entities.UniqueData;
import dao.entities.UniqueWrapper;
import dao.entities.UserInfo;
import main.Chuu;
import main.exceptions.LastFmException;
import main.parsers.OnlyUsernameParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class ProfileInfoCommand extends ConcurrentCommand {
	public ProfileInfoCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new OnlyUsernameParser(dao);
		this.respondInPrivate = false;
	}

	@Override
	String getDescription() {
		return "Brief description of user Profile";
	}

	@Override
	List<String> getAliases() {
		return Collections.singletonList("profile");
	}

	@Override
	protected void onCommand(MessageReceivedEvent e) {
		String[] returned = parser.parse(e);
		if (returned == null)
			return;
		String lastFmName = returned[0];
		//long discordID = Long.parseLong(returned[1]);
		UserInfo userInfo;
		int albumCount;
		try {
			userInfo = lastFM.getUserInfo(Collections.singletonList(lastFmName)).get(0);
			albumCount = lastFM.getTotalAlbumCount(lastFmName);

		} catch (LastFmException ex) {
			Chuu.getLogger().warn(ex.getMessage(), ex);
			sendMessage(e, "An error happened while processing the command");
			return;
		}
		UniqueWrapper<UniqueData> crowns = getDao().getCrowns(lastFmName, e.getGuild().getIdLong());
		UniqueWrapper<UniqueData> unique = getDao().getUniqueArtist(e.getGuild().getIdLong(), lastFmName);
		int totalUnique = unique.getRows();
		int totalCrowns = crowns.getRows();
		int totalArtist = getDao().getUserArtistCount(lastFmName);
		String crownRepresentative = !crowns.getUniqueData().isEmpty() ? crowns.getUniqueData().get(0)
				.getArtistName() : "not crowns";
		String UniqueRepresentative = !unique.getUniqueData().isEmpty() ? unique.getUniqueData().get(0)
				.getArtistName() : "not unique artists";

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Total Number of scrobbles: ").append(userInfo.getPlayCount()).append("\n")
				.append("Total Number of albums: ").append(albumCount).append("\n")
				.append("Total Number of artists: ").append(totalArtist).append("\n")
				.append("Total Number of crowns: ").append(totalCrowns).append("\n")
				.append("\tTop Crown:").append(crownRepresentative).append("\n")
				.append("Total Number of unique artist: ").append(totalUnique).append("\n")
				.append("\tTop unique:").append(UniqueRepresentative).append("\n");

		String name = getUserString(unique.getDiscordId(), e, lastFmName);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		String date = LocalDateTime.ofEpochSecond(userInfo.getUnixtimestamp(), 0, ZoneOffset.UTC)
				.format(formatter);

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle(name + "'s profile", CommandUtil.getLastFmUser(lastFmName))
				.setColor(CommandUtil.randomColor())
				.setThumbnail(userInfo.getImage().isEmpty() ? null : userInfo.getImage())
				.setDescription(stringBuilder)
				.setFooter("Account created on  " + date);

		MessageBuilder mes = new MessageBuilder();
		mes.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
	}

	@Override
	String getName() {
		return "Profile";
	}
}
