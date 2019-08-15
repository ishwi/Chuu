package main.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomAlbumParser extends Parser {

	private final Pattern spotify = Pattern
			.compile("^(https://open.spotify.com/(album|artist|track|playlist)/|spotify:(album|artist|track|playlist):)([a-zA-Z0-9]+)(.*)$");

	private final Pattern youtubePattern = Pattern
			.compile("http(?:s?)://(?:www\\.)?youtu(?:be\\.com/watch\\?v=|\\.be/)([\\w\\-_]*)(&(amp;)?[\\w?\u200C\u200B=]*)?");
	private final Pattern deezerPattern = Pattern
			.compile("^https?://(?:www\\.)?deezer\\.com/(track|album|playlist)/(\\d+)$/\n");


	@Override
	protected void setUpErrorMessages() {
		errorMessages.put(1, "Invalid url, only accepts spotify uri or url, yt url or deezer's url ");
	}

	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {
		if (subMessage == null || subMessage.length == 0)
			return new String[]{};
		String url = subMessage[0];
		Matcher matches;
		String group;

		if ((matches = spotify.matcher(url)).matches()) {
			group = "spotify";
			if (!url.startsWith("https:")) {
				String id = matches.group(4);
				String param = matches.group(3);
				url = "https://open.spotify.com/" + param + "/" + id;
			} else
				url = url.split("\\?si=")[0];

		} else if ((matches = youtubePattern.matcher(url)).matches()) {
			group = "yt";
			if (matches.group(2) != null) {
				url = url.split(matches.group(2))[0];
			}
		} else if ((matches = deezerPattern.matcher(url)).matches()) {
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
				"\t if no link is provided you get a random link\n";
	}
}

