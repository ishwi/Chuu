package main.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomAlbumParser extends Parser {
	private final Pattern mediaPattern = Pattern.compile("spotify:(artist|album|track):([0-9A-Za-z]{22})");
	private final Pattern playlistPattern = Pattern.compile("spotify:user:([^:]+):playlist:([0-9A-Za-z]{22})");
	private final Pattern youtubePattern = Pattern
			.compile("(?:https?:\\/\\/)?(?:youtu\\.be\\/|(?:www\\.|m\\.)?youtube\\.com\\/(?:watch|v|embed)(?:\\.php)?(?:\\?.*v=|\\/))([a-zA-Z0-9\\-_]+)");
	private final Pattern deezerPattern = Pattern
			.compile("/^https?:\\/\\/(?:www\\.)?deezer\\.com\\/(track|album|playlist)\\/(\\d+)$/\n");


	@Override
	protected void setUpErrorMessages() {
		errorMessages.put(1, "Invalid url, only accepts spotify uri or url, yt url or deezer's url ");
	}

	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {
		if (subMessage == null || subMessage.length == 0)
			return new String[]{};
		String url = subMessage[0];
		Matcher matches = mediaPattern.matcher(url);
		String group = "url";
		if (matches.matches()) {
			group = matches.group(1);

		} else if (playlistPattern.matcher(url).matches()) {
			group = "playlist";

		} else if (youtubePattern.matcher(url).matches()) {
			group = "yt";
		} else if (deezerPattern.matcher(url).matches()) {
			group = "deezer";
		} else {
			sendError(getErrorMessage(1), e);
			return null;
		}

		return new String[]{url, group};
	}


	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *url***\n" +
				"\t if no url is provided you get a random link\n" +
				"\t User needs to have administration permissions\n";
	}
}

