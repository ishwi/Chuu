package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.UniqueData;
import DAO.Entities.UniqueWrapper;
import DAO.Entities.UserInfo;
import main.Chuu;
import main.Exceptions.LastFmException;
import main.Parsers.OnlyUsernameParser;
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
	protected void threadableCode(MessageReceivedEvent e) {
		String[] returned = parser.parse(e);
		if (returned == null) {
			return;
		}
		String username = returned[0];
		UserInfo userInfo;
		int albumCount;
		try {
			userInfo = lastFM.getUserInfo(Collections.singletonList(username)).get(0);
			albumCount = lastFM.getTotalAlbumCount(username);

		} catch (LastFmException ex) {
			Chuu.getLogger().warn(ex.getMessage(), ex);
			return;
		}
		UniqueWrapper<UniqueData> crowns = getDao().getCrowns(username, e.getGuild().getIdLong());
		UniqueWrapper<UniqueData> unique = getDao().getUniqueArtist(e.getGuild().getIdLong(), username);
		int totalUnique = unique.getRows();
		int totalCrowns = crowns.getRows();
		int totalArtist = getDao().getUserArtistCount(username);
		String crownRepresentative = !crowns.getUniqueData().isEmpty() ? crowns.getUniqueData().get(0)
				.getArtistName() : "not crowns";
		String UniqueRepresentative = !unique.getUniqueData().isEmpty() ? unique.getUniqueData().get(0)
				.getArtistName() : "not unique artists";
		EmbedBuilder embedBuilder = new EmbedBuilder();
		String name = getUserString(unique.getDiscordId(), e, username);
		embedBuilder.setTitle(name + "'s profile", "https://www.last.fm/user/" + username);
		embedBuilder.setColor(CommandUtil.randomColor());
		embedBuilder.setThumbnail(userInfo.getImage().isEmpty() ? null : userInfo.getImage());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		String date = LocalDateTime.ofEpochSecond(userInfo.getUnixtimestamp(), 0, ZoneOffset.UTC)
				.format(formatter);

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("Total Number of scrobbles: ").append(userInfo.getPlayCount()).append("\n")
				.append("Total Number of albums: ").append(albumCount).append("\n")
				.append("Total Number of artists: ").append(totalArtist).append("\n")
				.append("Total Number of crowns: ").append(totalCrowns).append("\n")
				.append("\tTop Crown:").append(crownRepresentative).append("\n")
				.append("Total Number of unique artist: ").append(totalUnique).append("\n")
				.append("\tTop unique:").append(UniqueRepresentative).append("\n");
		embedBuilder.setDescription(stringBuilder);
		embedBuilder.setFooter("Account created on  " + date);
		MessageBuilder mes = new MessageBuilder();
		mes.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
	}

	@Override
	String getDescription() {
		return "Brief description of user Profile";
	}

	@Override
	String getName() {
		return "Profile";
	}

	@Override
	List<String> getAliases() {
		return Collections.singletonList("!profile");
	}
}
