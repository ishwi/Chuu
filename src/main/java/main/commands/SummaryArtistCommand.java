package main.commands;

import dao.DaoImplementation;
import dao.entities.ArtistSummary;
import dao.entities.LastFMData;
import main.apis.discogs.DiscogsApi;
import main.apis.discogs.DiscogsSingleton;
import main.apis.spotify.Spotify;
import main.apis.spotify.SpotifySingleton;
import main.exceptions.LastFmEntityNotFoundException;
import main.exceptions.LastFmException;
import main.parsers.ArtistParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SummaryArtistCommand extends ConcurrentCommand {

	private final Spotify spotify;
	private final DiscogsApi discogsApi;

	public SummaryArtistCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new ArtistParser(dao, lastFM);
		this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
		this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
	}

	@Override
	String getDescription() {
		return "Brief summary of an artist";
	}

	@Override
	List<String> getAliases() {
		return Arrays.asList("artistinfo", "ai");
	}

	@Override
	void onCommand(MessageReceivedEvent e) {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;

		final String artist = CommandUtil.onlyCorrection(getDao(), returned[0], lastFM);
		long whom = Long.parseLong(returned[1]);
		ArtistSummary summary;
		LastFMData data;
		try {
			data = getDao().findLastFMData(whom);
			summary = lastFM.getArtistSummary(artist, data.getName());
			if (summary == null) {
				parser.sendError(artist + " doesn't exist on Last.fm", e);
				return;
			}
		} catch (InstanceNotFoundException e1) {
			parser.sendError(parser.getErrorMessage(3), e);
			return;
		} catch (LastFmEntityNotFoundException ex) {
			//parser.sendError(parser.getErrorMessage(2), e);
			parser.sendError(artist + " doesn't exist on Last.fm", e);
			return;
		} catch (LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
			return;
		}

		String username = getUserStringConsideringGuildOrNot(e, whom, data.getName());
		EmbedBuilder embedBuilder = new EmbedBuilder();
		String tagsField = summary.getTags().stream()
				.map(tag -> "[" + tag + "](" + CommandUtil.getLastFmTagUrl(tag) + ")")
				.collect(Collectors.joining(" - "));
		String similarField = summary.getSimilars().stream()
				.map(art -> "[" + art + "](" + CommandUtil.getLastFmArtistUrl(art) + ")")
				.collect(Collectors.joining(" - "));

		MessageBuilder messageBuilder = new MessageBuilder();
		embedBuilder.setTitle("Information about " + artist, CommandUtil.getLastFmArtistUrl(artist))
				.addField(username + "'s plays:", String.valueOf(summary.getUserPlayCount()), true)
				.addField("Listeners:", String.valueOf(summary.getListeners()), true)
				.addField("Scrobbles:", String.valueOf(summary.getPlaycount()), true)
				.addField("Tags:", tagsField, false)
				.addField("Similars:", similarField, false)
				.addField("Bio:", summary.getSummary(), false)
				.setImage(CommandUtil.getArtistImageUrl(getDao(), artist, lastFM, discogsApi, spotify))
				.setColor(CommandUtil.randomColor());
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
	}

	@Override
	String getName() {
		return "Artist Info";
	}
}
