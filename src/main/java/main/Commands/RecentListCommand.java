package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Parsers.OnlyUsernameParser;
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
	protected void threadableCode(MessageReceivedEvent e) {

		String[] returned = parser.parse(e);
		if (returned == null) {
			return;
		}
		int limit = 5;
		String username = returned[0];
		try {
			long discordId = getDao().getDiscordIdFromLastfm(username, e.getGuild().getIdLong());

			List<NowPlayingArtist> list = lastFM.getRecent(username, limit);
			StringBuilder a = new StringBuilder();
			a.append("\n");
			NowPlayingArtist header = list.get(0);


			String name = getUserString(discordId, e, username);
			int counter = 1;
			for (NowPlayingArtist nowPlayingArtist : list) {
				a.append("**Track #").append(counter++)
						.append(":**\n")
						.append(nowPlayingArtist.getArtistName())
						.append(" - ").append(nowPlayingArtist.getSongName())
						.append(" | ").append(nowPlayingArtist.getAlbumName()).append("\n\n");
			}

			EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor()).setThumbnail(CommandUtil.noImageUrl(header.getUrl()))
					.setTitle("** " + name + "'s last " + limit + " tracks **",
							"https://www.last.fm/user/" + username)
					.setDescription(a);

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
	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!recent");
	}

	@Override
	public String getDescription() {
		return "Returns your most recent songs played";
	}

	@Override
	public String getName() {
		return "Recent";
	}


}
