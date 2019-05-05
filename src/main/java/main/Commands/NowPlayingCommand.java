package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmException;
import main.Exceptions.ParseException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
public class NowPlayingCommand extends ConcurrentCommand {
	public NowPlayingCommand(DaoImplementation dao) {
		super(dao);
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
		return Collections.singletonList("!np *username \n" +
				"\t If useranme is not specified defaults to authors account\n\n");
	}


	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		String[] message = getSubMessage(e.getMessage());
		return new String[]{getLastFmUsername1input(message, e.getAuthor().getIdLong(), e)};

	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request:\n";
		String message;
		if (code == 0) {
			userNotOnDB(e, code);
			return;
		}

		if (code == 2) {
			message = "User hasnt played any song recently!";

		} else {
			message = "There was a problem with Last FM Api " + cause;
		}
		sendMessage(e, base + message);
	}

	@Override
	public void threadableCode() {
		try {
			String username = parse(e)[0];
			NowPlayingArtist nowPlayingArtist = lastFM.getNowPlayingInfo(username);
			StringBuilder a = new StringBuilder();

			a.append("**[").append(username).append("'s Profile](").append("https://www.last.fm/user/").append(username).append(")**\n\n")
					.append(nowPlayingArtist.isNowPlaying() ? "Current" : "Last")
					.append(":\n")
					.append("**").append(nowPlayingArtist.getSongName()).append("**")
					.append(" - ").append(nowPlayingArtist.getAlbumName()).append(" | ")
					.append(nowPlayingArtist.getArtistName());

			EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor()).setThumbnail(CommandUtil.noImageUrl(nowPlayingArtist.getUrl()))
					.setTitle("Now Playing:")
					.setDescription(a);

			MessageBuilder messageBuilder = new MessageBuilder();
			messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();

		} catch (ParseException ex) {
			errorMessage(e, 0, ex.getMessage());
		} catch (LastFMNoPlaysException e1) {
			errorMessage(e, 2, e1.getMessage());

		} catch (LastFmException ex) {
			errorMessage(e, 1, ex.getMessage());

		}
	}
}
