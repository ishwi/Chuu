package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.last.ConcurrentLastFM;
import main.last.LastFMServiceException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
public class NowPlayingCommand extends MyCommandDbAccess {
	public NowPlayingCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {
		try {
			String username = parse(e)[0];
			NowPlayingArtist nowPlayingArtist = ConcurrentLastFM.getNowPlayingInfo(username);
			StringBuilder a = new StringBuilder();
			e.getChannel().sendTyping().queue();

			a.append("**[").append(username).append("'s Profile](").append("https://www.last.fm/user/").append(username).append(")**\n\n")
					.append(nowPlayingArtist.isNowPlaying() ? "Current" : "Last")
					.append(":\n")
					.append("**").append(nowPlayingArtist.getSongName()).append("**")
					.append(" - ").append(nowPlayingArtist.getAlbumName()).append(" | ")
					.append(nowPlayingArtist.getArtistName());

			EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor()).setThumbnail(nowPlayingArtist.getUrl())
					.setTitle("Now Playing:")
					.setDescription(a);

			MessageBuilder messageBuilder = new MessageBuilder();
			messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();

		} catch (ParseException ex) {
			userNotOnDB(e);
		} catch (LastFMServiceException ex) {
			onLastFMError(e);
		}

	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!np");
	}

	@Override
	public String getDescription() {
		return "Returns your current playing song";
	}

	@Override
	public String getName() {
		return "Now Playing";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("**!np *username **\n" +
				"\t If useranme is not specified defaults to authors account\n\n");
	}


	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		String[] message = getSubMessage(e.getMessage());
		return new String[]{getLastFmUsername1input(message, e.getAuthor().getIdLong(), e)};

	}
}
