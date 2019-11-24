package main.commands;

import dao.DaoImplementation;
import dao.entities.*;
import main.apis.discogs.DiscogsApi;
import main.apis.discogs.DiscogsSingleton;
import main.apis.spotify.Spotify;
import main.apis.spotify.SpotifySingleton;
import main.exceptions.InstanceNotFoundException;
import main.exceptions.LastFmException;
import main.imagerenderer.ProfileMaker;
import main.parsers.OnlyUsernameParser;
import main.parsers.OptionalEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class ProfileInfoCommand extends ConcurrentCommand {
	private final Spotify spotify;
	private final DiscogsApi discogsApi;

	public ProfileInfoCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new OnlyUsernameParser(dao, new OptionalEntity("--image", "display in list format"));
		this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
		this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
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
	protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned = parser.parse(e);
		if (returned == null)
			return;
		String lastFmName = returned[0];
		//long discordID = Long.parseLong(returned[1]);
		boolean isList = !Boolean.parseBoolean(returned[2]);
		UserInfo userInfo;
		int albumCount;

		userInfo = lastFM.getUserInfo(Collections.singletonList(lastFmName)).get(0);
			albumCount = lastFM.getTotalAlbumCount(lastFmName);

		UniqueWrapper<UniqueData> crowns = getDao().getCrowns(lastFmName, e.getGuild().getIdLong());
		UniqueWrapper<UniqueData> unique = getDao().getUniqueArtist(e.getGuild().getIdLong(), lastFmName);
		ObscuritySummary summary = getDao().getObscuritySummary(lastFmName);

		int totalUnique = unique.getRows();
		int totalCrowns = crowns.getRows();
		int totalArtist = getDao().getUserArtistCount(lastFmName);
		String crownRepresentative = !crowns.getUniqueData().isEmpty() ? crowns.getUniqueData().get(0)
				.getArtistName() : "no crowns";
		String UniqueRepresentative = !unique.getUniqueData().isEmpty() ? unique.getUniqueData().get(0)
				.getArtistName() : "no unique artists";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		String date = LocalDateTime.ofEpochSecond(userInfo.getUnixtimestamp(), 0, ZoneOffset.UTC)
				.format(formatter);
		if (isList) {

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Total number of scrobbles: ").append(userInfo.getPlayCount()).append("\n")
					.append("Total number of albums: ").append(albumCount).append("\n")
					.append("Total number of artists: ").append(totalArtist).append("\n")
					.append("Total number of crowns: ").append(totalCrowns).append("\n")
					.append("Top crown:").append(crownRepresentative).append("\n")
					.append("Total number of unique artist: ").append(totalUnique).append("\n")
					.append("Top unique:").append(UniqueRepresentative).append("\n");

			String name = getUserString(unique.getDiscordId(), e, lastFmName);

			EmbedBuilder embedBuilder = new EmbedBuilder()
					.setTitle(name + "'s profile", CommandUtil.getLastFmUser(lastFmName))
					.setColor(CommandUtil.randomColor())
					.setThumbnail(userInfo.getImage().isEmpty() ? null : userInfo.getImage())
					.setDescription(stringBuilder)
					.setFooter("Account created on " + date);

			MessageBuilder mes = new MessageBuilder();
			mes.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();

		} else {

			String crownImage = !crowns.getUniqueData().isEmpty() ?
					CommandUtil
							.getArtistImageUrl(getDao(), crownRepresentative, lastFM, discogsApi, spotify)
					: null;

			String uniqueImage = !unique.getUniqueData().isEmpty() ? CommandUtil
					.getArtistImageUrl(getDao(), UniqueRepresentative, lastFM, discogsApi, spotify) : null;

			ProfileEntity entity = new ProfileEntity(lastFmName, "", crownRepresentative, UniqueRepresentative, uniqueImage, crownImage, userInfo
					.getImage(), "", userInfo
					.getPlayCount(), albumCount, totalArtist, totalCrowns, totalUnique, summary.getTotal(), date);
			sendImage(ProfileMaker.makeProfile(entity), e);
		}
	}

	@Override
	String getName() {
		return "Profile";
	}

	private void generateImage() {

	}
}
