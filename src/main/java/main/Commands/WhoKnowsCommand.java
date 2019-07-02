package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.WrapperReturnNowPlaying;
import main.APIs.Discogs.DiscogsApi;
import main.APIs.Discogs.DiscogsSingleton;
import main.APIs.Spotify.Spotify;
import main.APIs.Spotify.SpotifySingleton;
import main.ImageRenderer.NPMaker;
import main.Parsers.WhoKnowsParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
		this.parser = new WhoKnowsParser();
		this.respondInPrivate = false;

	}

	void whoKnowsLogic(String who, Boolean isImage, MessageReceivedEvent e) {
		MessageBuilder messageBuilder = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();

		ArtistData validable = new ArtistData(who, 0, "");

		CommandUtil.lessHeavyValidate(getDao(), validable, lastFM, discogsApi, spotify);

		WrapperReturnNowPlaying wrapperReturnNowPlaying = this.getDao().whoKnows(validable.getArtist(), e.getGuild().getIdLong());
		//With db cache?? Extra
		if (wrapperReturnNowPlaying.getRows() == 0) {
			messageBuilder.setContent("No nibba listens to " + who).sendTo(e.getChannel()).queue();
			return;
		}
		wrapperReturnNowPlaying.setUrl(validable.getUrl());

		if (!isImage) {
			StringBuilder builder = new StringBuilder();
			int counter = 1;
			for (ReturnNowPlaying returnNowPlaying : wrapperReturnNowPlaying.getReturnNowPlayings()) {

				String userName = getUserString(returnNowPlaying.getDiscordId(), e, returnNowPlaying.getLastFMId());
				builder.append(counter++)
						.append(". ")
						.append("[").append(userName).append("]")
						.append("(https://www.last.fm/user/").append(returnNowPlaying.getLastFMId())
						.append("/library/music/").append(wrapperReturnNowPlaying.getArtist().replaceAll(" ", "+").replaceAll("[)]", "%29")).append(") - ")
						.append(returnNowPlaying.getPlayNumber()).append(" plays\n");
			}

			embedBuilder.setTitle("Who knows " + who + " in " + e.getGuild().getName() + "?").
					setThumbnail(CommandUtil.noImageUrl(wrapperReturnNowPlaying.getUrl())).setDescription(builder)
					.setColor(CommandUtil.randomColor());
			//.setFooter("Command invoked by " + event.getMember().getLastFmId().getDiscriminator() + "" + LocalDateTime.now().format(DateTimeFormatter.ISO_WEEK_DATE).toString(), );
			messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).submit();
			return;
		}

		wrapperReturnNowPlaying.getReturnNowPlayings().forEach(element ->
				element.setDiscordName(getUserString(element.getDiscordId(), e, element.getLastFMId()))
		);
		BufferedImage logo = CommandUtil.getLogo(getDao(), e);
		BufferedImage image = NPMaker.generateTasteImage(wrapperReturnNowPlaying, e.getGuild().getName(), logo);
		sendImage(image, e);
	}


	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;

		whoKnowsLogic(returned[0], Boolean.valueOf(returned[1]), e);

	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("!whoknows", "!wk");
	}

	@Override
	public String getDescription() {
		return "Returns List Of Users Who Know the inputted Artist";
	}

	@Override
	public String getName() {
		return "Who Knows";
	}


}
