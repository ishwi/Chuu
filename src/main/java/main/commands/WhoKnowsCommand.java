package main.commands;

import dao.DaoImplementation;
import dao.entities.ArtistData;
import dao.entities.ReturnNowPlaying;
import dao.entities.WrapperReturnNowPlaying;
import main.apis.discogs.DiscogsApi;
import main.apis.discogs.DiscogsSingleton;
import main.apis.spotify.Spotify;
import main.apis.spotify.SpotifySingleton;
import main.exceptions.LastFmException;
import main.imagerenderer.WhoKnowsMaker;
import main.otherlisteners.Reactionary;
import main.parsers.ArtistParser;
import main.parsers.OptionalEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;


public class WhoKnowsCommand extends ConcurrentCommand {
	private final DiscogsApi discogsApi;
	private final Spotify spotify;


	public WhoKnowsCommand(DaoImplementation dao) {
		super(dao);
		this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
		this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
		this.parser = new ArtistParser(dao, lastFM, new OptionalEntity("--list", "display in list format"));
		this.respondInPrivate = false;

	}

	@Override
	public String getDescription() {
		return "Returns List Of Users Who Know the inputted Artist";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("whoknows", "wk", "whoknowsnp", "wknp");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;
		ArtistData validable = new ArtistData(returned[0], 0, "");
		CommandUtil.lessHeavyValidate(getDao(), validable, lastFM, discogsApi, spotify);
		whoKnowsLogic(validable, Boolean.parseBoolean(returned[2]), e, Long.parseLong(returned[1]));

	}

	void whoKnowsLogic(ArtistData who, Boolean isList, MessageReceivedEvent e, long userId) {
		MessageBuilder messageBuilder = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();

		WrapperReturnNowPlaying wrapperReturnNowPlaying =
				isList
						? this.getDao().whoKnows(who.getArtist(), e.getGuild().getIdLong(), Integer.MAX_VALUE)
						: this.getDao().whoKnows(who.getArtist(), e.getGuild().getIdLong());
		if (wrapperReturnNowPlaying.getRows() == 0) {
			messageBuilder.setContent("No one knows " + who.getArtist()).sendTo(e.getChannel()).queue();
			return;
		}
		wrapperReturnNowPlaying.setUrl(who.getUrl());

		if (isList) {
			wrapperReturnNowPlaying.getReturnNowPlayings()
					.forEach(x -> x.setDiscordName(getUserString(x.getDiscordId(), e, x.getLastFMId())));

			StringBuilder builder = new StringBuilder();
			int counter = 1;
			for (ReturnNowPlaying returnNowPlaying : wrapperReturnNowPlaying.getReturnNowPlayings()) {
				builder.append(counter++)
						.append(returnNowPlaying.toString());

				if (counter == 11)
					break;
			}

			embedBuilder.setTitle("Who knows " + who.getArtist() + " in " + e.getGuild().getName() + "?").
					setThumbnail(CommandUtil.noImageUrl(wrapperReturnNowPlaying.getUrl())).setDescription(builder)
					.setColor(CommandUtil.randomColor());
			//.setFooter("Command invoked by " + event.getMember().getLastFmId().getDiscriminator() + "" + LocalDateTime.now().format(DateTimeFormatter.ISO_WEEK_DATE).toApiFormat(), );
			messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel())
					.queue(message1 -> new Reactionary<>(wrapperReturnNowPlaying
							.getReturnNowPlayings(), message1, embedBuilder));
		} else {

			wrapperReturnNowPlaying.getReturnNowPlayings().forEach(element ->
					element.setDiscordName(getUserString(element.getDiscordId(), e, element.getLastFMId()))
			);
			BufferedImage logo = CommandUtil.getLogo(getDao(), e);
			BufferedImage image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, e.getGuild().getName(), logo);
			sendImage(image, e);

		}
	}

	@Override
	public String getName() {
		return "Who Knows";
	}


}
