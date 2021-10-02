package main.commands;

import dao.DaoImplementation;
import dao.entities.NowPlayingArtist;
import main.exceptions.LastFMNoPlaysException;
import main.exceptions.LastFmEntityNotFoundException;
import main.exceptions.LastFmException;
import main.parsers.OnlyUsernameParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class RecentListCommand extends ConcurrentCommand {
	private final int LIMIT = 5;

	public RecentListCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new OnlyUsernameParser(dao);
	}

	@Override
	public String getDescription() {
		return "Returns your most recent songs played";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("recent");
	}

	@Override
	protected void onCommand(MessageReceivedEvent e) {

		String[] returned = parser.parse(e);
		if (returned == null) {
			return;
		}
		String lastFmName = returned[0];
		long discordID = Long.parseLong(returned[1]);
		String usable = getUserStringConsideringGuildOrNot(e, discordID, lastFmName);
		try {

			List<NowPlayingArtist> list = lastFM.getRecent(lastFmName, LIMIT);
			//Can't be empty because NoPLaysException
			NowPlayingArtist header = list.get(0);

			EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
					.setThumbnail(CommandUtil.noImageUrl(header.getUrl()))
					.setTitle("** " + usable + "'s last " + LIMIT + " tracks **",
							CommandUtil.getLastFmUser(lastFmName));

			int counter = 1;
			for (NowPlayingArtist nowPlayingArtist : list) {
				embedBuilder.addField("Track #" + counter++ + ":", "**" + nowPlayingArtist.getSongName() +
						"**- " + nowPlayingArtist.getArtistName() + " | " + nowPlayingArtist
						.getAlbumName() + "\n", false);
			}

			MessageBuilder messageBuilder = new MessageBuilder();
			messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();


		} catch (LastFMNoPlaysException e1) {
			parser.sendError(parser.getErrorMessage(3), e);
		} catch (LastFmEntityNotFoundException e2) {
			parser.sendError(parser.getErrorMessage(4), e);
		} catch (LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
		}

	}

	@Override
	public String getName() {
		return "Recent";
	}


}
