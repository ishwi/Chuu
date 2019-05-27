package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.APIs.Youtube.Search;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmException;
import main.Exceptions.ParseException;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request:\n";
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
		sendMessage(e, base + message);
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
			NowPlayingArtist nowPlayingArtist = lastFM.getNowPlayingInfo(username);
			String uri = search.doSearch(nowPlayingArtist.getSongName() + " " + nowPlayingArtist.getArtistName());


			if (uri.equals("")) {
				System.out.println("Doing a second attempt");
				uri = search.doSearch(nowPlayingArtist.getSongName() + " " + nowPlayingArtist.getArtistName());
				if (uri.equals("")) {
					errorMessage(e, 1, "Youtube");
					return;
				}
			}
			messageBuilder.setContent(uri).sendTo(e.getChannel()).queue();
		} catch (LastFMNoPlaysException e1) {
			errorMessage(e, 3, e1.getMessage());

		} catch (LastFmException ex) {
			errorMessage(e, 2, ex.getMessage());
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
		return Collections.singletonList("!npyoutube *username  \n \tIf not specified another user it defaults to yours");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		return new String[]{getLastFmUsername1input(getSubMessage(e.getMessage()), e.getAuthor().getIdLong(), e)};
	}
}
