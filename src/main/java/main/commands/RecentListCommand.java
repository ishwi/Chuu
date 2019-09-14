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

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;

public class RecentListCommand extends ConcurrentCommand {
	public RecentListCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new OnlyUsernameParser(dao);
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("recent");
	}

	@Override
	public String getDescription() {
		return "Returns your most recent songs played";
	}

	@Override
	public String getName() {
		return "Recent";
	}

	@Override
	protected void onCommand(MessageReceivedEvent e) {

		String[] returned = parser.parse(e);
		if (returned == null) {
			return;
		}
		int limit = 5;
		String username = returned[0];
		try {
			long discordId = getDao().getDiscordIdFromLastfm(username, e.getGuild().getIdLong());

			List<NowPlayingArtist> list = lastFM.getRecent(username, limit);
			NowPlayingArtist header = list.get(0);

			String name = getUserString(discordId, e, username);
			int counter = 1;
			EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
					.setThumbnail(CommandUtil.noImageUrl(header.getUrl()))
					.setTitle("** " + name + "'s last " + limit + " tracks **",
							CommandUtil.getLastFmUser(username));

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
		} catch (InstanceNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(1), e);
		}

	}


}
