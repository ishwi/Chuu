package core.commands;

import dao.DaoImplementation;
import dao.entities.ArtistSummary;
import dao.entities.LastFMData;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
	public String getDescription() {
		return "Brief summary of an artist";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("artistinfo", "ai");
	}

	@Override
	public String getName() {
		return "Artist Info";
	}

	@Override
	void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;

		final String artist = CommandUtil.onlyCorrection(getDao(), returned[0], lastFM);
		long whom = Long.parseLong(returned[1]);
		LastFMData data = getDao().findLastFMData(whom);
		ArtistSummary summary = lastFM.getArtistSummary(artist, data.getName());

		String username = getUserStringConsideringGuildOrNot(e, whom, data.getName());
		EmbedBuilder embedBuilder = new EmbedBuilder();
		String tagsField = summary.getTags().isEmpty()
				? ""
				: summary.getTags().stream()
				.map(tag -> "[" + tag + "](" + CommandUtil.getLastFmTagUrl(tag) + ")")
				.collect(Collectors.joining(" - "));

		String similarField =
				summary.getSimilars().isEmpty()
						? ""
						: summary.getSimilars().stream()
						.map(art -> "[" + art + "](" + CommandUtil.getLastFmArtistUrl(art) + ")")
						.collect(Collectors.joining(" - "));

		String artistImageUrl = CommandUtil.getArtistImageUrl(getDao(), artist, lastFM, discogsApi, spotify);
		MessageBuilder messageBuilder = new MessageBuilder();
		embedBuilder.setTitle("Information about " + summary.getArtistname(), CommandUtil.getLastFmArtistUrl(artist))
				.addField(username + "'s plays:", String.valueOf(summary.getUserPlayCount()), true)
				.addField("Listeners:", String.valueOf(summary.getListeners()), true)
				.addField("Scrobbles:", String.valueOf(summary.getPlaycount()), true)
				.addField("Tags:", tagsField, false)
				.addField("Similars:", similarField, false)
				.addField("Bio:", summary.getSummary(), false)
				.setImage(artistImageUrl.isEmpty() ? null : artistImageUrl)
				.setColor(CommandUtil.randomColor());
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
	}
}
