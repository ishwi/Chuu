package core.parsers;

import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.UrlExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.RandomUrlParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomAlbumParser extends DaoParser<RandomUrlParameters> {

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

    public RandomAlbumParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    void setUpOptionals() {
        super.setUpOptionals();
        this.opts.add(new OptionalEntity("server", "only include urls from people in this server"));
    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(1, "Invalid url, only accepts spotify uri or url, yt url, deezer's url,bandcamp's and  soundcloud's url");
    }

    public RandomUrlParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        ParserAux parserAux = new ParserAux(subMessage);
        User oneUser = parserAux.getOneUser(e, dao);
        subMessage = parserAux.getMessage();

        if (subMessage == null || subMessage.length == 0)
            return new RandomUrlParameters(e, "", oneUser);
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

        return new RandomUrlParameters(e, url, oneUser);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new UrlExplanation() {
            @Override
            public ExplanationLine explanation() {
                return new ExplanationLine(super.explanation().header(),
                        """
                                If no link is provided you'll get a random link
                                The allowed platforms for links are: Spotify,Youtube,Bandcamp,Deezer and Soundcloud
                                """);
            }
        }, new StrictUserExplanation());
    }


}

