package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFMServiceException;
import main.Exceptions.ParseException;
import main.Youtube.Search;
import main.last.ConcurrentLastFM;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NPYoutubeCommand extends MyCommandDbAccess {
	private Search search;

	public NPYoutubeCommand(DaoImplementation dao) {
		super(dao);
		this.search = new Search();

	}

	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String message;
		switch (code) {
			case 0:
				message = "User Was not found on the database";
				break;
			case 1:
				message = "Didnt find what you were playing on Youtube";
				break;
			case 2:
				message = "There was a problem with Last FM Api" + cause;
				break;
			case 3:
				message = "User hasnt played any song recently!";
				break;
			default:
				message = "An unkown error happened while processing your request";
		}
		sendMessage(e, message);
	}

	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {
		String username;
		MessageBuilder messageBuilder = new MessageBuilder();

		try {
			username = parse(e)[0];
		} catch (ParseException ex) {
			errorMessage(e, 0, ex.getMessage());
			return;
		}
		try {
			NowPlayingArtist nowPlayingArtist = ConcurrentLastFM.getNowPlayingInfo(username);
			StringBuilder a = new StringBuilder();
			e.getChannel().sendTyping().queue();
			String uri = search.doSearch(nowPlayingArtist.getSongName() + " " + nowPlayingArtist.getArtistName());

			if (uri.equals("")) {
				errorMessage(e, 1, "Youtube");
				return;
			}
			messageBuilder.setContent(uri).sendTo(e.getChannel()).queue();
		} catch (LastFMServiceException ex) {
			errorMessage(e, 2, ex.getMessage());
		} catch (LastFMNoPlaysException e1) {
			errorMessage(e, 3, e1.getMessage());
		}

	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("!npyoutube", "!npyt", "!yt", "!npyou");
	}

	@Override
	public String getDescription() {
		return "Returns a link to your current song via Youtube";
	}

	@Override
	public String getName() {
		return "Now Playing Youtube";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("If not specified another user it defaults to yours");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		return new String[]{getLastFmUsername1input(getSubMessage(e.getMessage()), e.getAuthor().getIdLong(), e)};
	}
}
