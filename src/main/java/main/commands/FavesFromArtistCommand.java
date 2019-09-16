package main.commands;

import dao.DaoImplementation;
import dao.entities.ArtistData;
import dao.entities.TimeFrameEnum;
import dao.entities.Track;
import main.apis.discogs.DiscogsApi;
import main.apis.discogs.DiscogsSingleton;
import main.apis.spotify.Spotify;
import main.apis.spotify.SpotifySingleton;
import main.exceptions.LastFMNoPlaysException;
import main.exceptions.LastFmException;
import main.parsers.ArtistTimeFrameParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Arrays;
import java.util.List;

public class FavesFromArtistCommand extends ConcurrentCommand {
	private final DiscogsApi discogsApi;
	private final Spotify spotify;

	public FavesFromArtistCommand(DaoImplementation dao) {
		super(dao);
		respondInPrivate = true;
		this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
		this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
		this.parser = new ArtistTimeFrameParser(dao, lastFM);
	}

	@Override
	public String getDescription() {
		return
				"Fav  tracks from an artist";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("favs", "favourites", "favorites");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;
		long userId = Long.parseLong(returned[1]);
		String timeframew = returned[2];
		ArtistData who = new ArtistData(returned[0], 0, "");
		CommandUtil.lessHeavyValidate(getDao(), who, lastFM, discogsApi, spotify);
		List<Track> ai;
		String lastFmName;
		try {
			lastFmName = getDao().findLastFMData(userId).getName();
		} catch (InstanceNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(1), e);
			return;
		}

		try {
			ai = lastFM.getTopArtistTracks(lastFmName, who.getArtist(), timeframew);
		} catch (LastFMNoPlaysException ex) {
			parser.sendError("No plays at all bro", e);
			return;
		} catch (LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
			return;
		}
		final String userString = getUserStringConsideringGuildOrNot(e, userId, lastFmName);
		if (ai.isEmpty()) {
			sendMessageQueue(e, " No faves on provided time!");
			return;
		}

		MessageBuilder mes = new MessageBuilder();
		StringBuilder s = new StringBuilder();

		for (int i = 0; i < 10 && i < ai.size(); i++) {
			Track g = ai.get(i);
			s.append(i + 1).append(": ").append(g.getName()).append(" - ").append(g.getPlays()).append(" plays")
					.append("\n");
		}
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setDescription(s);
		embedBuilder.setColor(CommandUtil.randomColor());

		embedBuilder
				.setTitle(userString + "'s Top " + who.getArtist() + " Tracks in " + TimeFrameEnum
						.fromCompletePeriod(timeframew).toString(), CommandUtil.getLastFmUser(lastFmName));
		embedBuilder.setThumbnail(CommandUtil.noImageUrl(who.getUrl()));

		e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue();
	}

	@Override
	public String getName() {

		return "Fav tracks";
	}
}
