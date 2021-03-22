package core.parsers;

import core.parsers.params.UrlParameters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomAlbumParser extends Parser<UrlParameters> {

    private final Pattern spotify = Pattern
            .compile("^(https://open.spotify.com/(album|artist|track|playlist)/|spotify:(album|artist|track|playlist):)([a-zA-Z0-9]{22})(?:\\?.*)?$");

    private final Pattern youtubePattern = Pattern
            .compile("(?:https?://)?(?:(?:www\\.)?|music\\.)?youtu\\.?be(?:\\.com)?/?.*(?:watch|embed)?(?:.*v=|v/|/)([\\w-_]{11}).*?$");
    private final Pattern deezerPattern = Pattern
            .compile("^https?://(?:www\\.)?deezer\\.com/(?:\\w+/)?(track|album|playlist)/(\\d+).*$");
    private final Pattern soundCloundPattern = Pattern
            .compile("((https://)|(http://)|(www.)|(m\\.)|(\\s))+(soundcloud.com/)+[a-zA-Z0-9\\-.]+(/)+[a-zA-Z0-9\\-.]+(/)?+[a-zA-Z0-9\\-.]+?");
    private final Pattern bandCampPatter = Pattern
            .compile("(http://(.*\\.bandcamp\\.com/|.*\\.bandcamp\\.com/track/.*|.*\\.bandcamp\\.com/album/.*))|(https://(.*\\.bandcamp\\.com/|.*\\.bandcamp\\.com/track/.*|.*\\.bandcamp\\.com/album/.*))");

    @Override
    void setUpOptionals() {
        super.setUpOptionals();
        this.opts.add(new OptionalEntity("server", "only include urls from people in this server"));
    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(1, "Invalid url, only accepts spotify uri or url, yt url, deezer's url,bandcamp's and  soundcloud's url");
    }

    public UrlParameters parseLogic(MessageReceivedEvent e, String[] subMessage) {
        if (subMessage == null || subMessage.length == 0)
            return new UrlParameters(e, "");
        else if (subMessage.length != 1) {
            sendError("Only one word was expected", e);
            return null;
        }
        String url = subMessage[0].trim();
        Matcher matches;

        if ((matches = spotify.matcher(url)).matches()) {
            String param;
            if (!url.startsWith("https:"))
                param = matches.group(3);
            else
                param = matches.group(2);

            String id = matches.group(4);
            //Normalizes the url
            url = "https://open.spotify.com/" + param + "/" + id;


        } else if ((matches = youtubePattern.matcher(url)).lookingAt()) {
            if (matches.group(1) != null) {
                url = "https://www.youtube.com/watch?v=" + matches.group(1);
            }
        } else if ((matches = deezerPattern.matcher(url)).matches()) {
            if (matches.group(1) != null && matches.group(2) != null) {
                url = "https://www.deezer.com/" + matches.group(1) + "/" + matches.group(2);
            }
        } else if (!soundCloundPattern.matcher(url).matches() && !bandCampPatter.matcher(url).matches()) {
            sendError(getErrorMessage(1), e);
            return null;
        }

        return new UrlParameters(e, url);
    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *url***\n" +
                "\t if no link is provided you get a random link\n" +
                "\t the accepted links so far are: Spotify's uri and url, youtube's url ,bandcamp's, deezer's url and  soundcloud's url\n";
    }
}

