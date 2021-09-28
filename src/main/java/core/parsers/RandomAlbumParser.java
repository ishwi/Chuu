package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.UrlExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.RandomUrlParameters;
import core.parsers.utils.OptionalEntity;
import core.parsers.utils.Optionals;
import dao.ChuuService;
import dao.entities.RandomTarget;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;
import java.util.regex.Matcher;

public class RandomAlbumParser extends DaoParser<RandomUrlParameters> {


    public RandomAlbumParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    public RandomUrlParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        User user = InteractionAux.parseUser(e);
        OptionMapping option = e.getOption(UrlExplanation.NAME);
        if (option == null) {
            return new RandomUrlParameters(ctx, "", user);
        } else {
            String url = option.getAsString();
            String[] words = url.split("\\s+");
            if (words.length != 1) {
                sendError("Only submit a link pls", ctx);
                return null;
            }
            return processUrl(url, ctx, user);
        }
    }

    @Override
    void setUpOptionals() {
        super.setUpOptionals();
        addOptional(Optionals.SERVER.opt.withDescription("only include urls from people in this server"));
    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(1, "Invalid url, only accepts spotify uri or url, yt url, deezer's url,bandcamp's and  soundcloud's url");
    }

    public RandomUrlParameters parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {
        ParserAux parserAux = new ParserAux(subMessage);
        User oneUser = parserAux.getOneUser(e, dao);
        subMessage = parserAux.getMessage();

        if (subMessage == null || subMessage.length == 0)
            return new RandomUrlParameters(e, "", oneUser);
        else if (subMessage.length != 1) {
            sendError("Only submit a link pls", e);
            return null;
        }
        return processUrl(subMessage[0], e, oneUser);
    }

    private RandomUrlParameters processUrl(String url, Context e, User oneUser) {
        url = url.trim();
        Matcher matches;

        if ((matches = RandomTarget.SPOTIFY.regex.matcher(url)).matches()) {
            String param;
            if (!url.startsWith("https:"))
                param = matches.group(3);
            else
                param = matches.group(2);

            String id = matches.group(4);
            //Normalizes the url
            url = "https://open.spotify.com/" + param + "/" + id;


        } else if ((matches = RandomTarget.YOUTUBE.regex.matcher(url)).lookingAt()) {
            if (matches.group(1) != null) {
                url = "https://www.youtube.com/watch?v=" + matches.group(1);
            }
        } else if ((matches = RandomTarget.DEEZER.regex.matcher(url)).matches()) {
            if (matches.group(1) != null && matches.group(2) != null) {
                url = "https://www.deezer.com/" + matches.group(1) + "/" + matches.group(2);
            }
        } else if (!RandomTarget.SOUNDCLOUD.regex.matcher(url).matches() && !RandomTarget.BANDCAMP.regex.matcher(url).matches()) {
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
                                """, super.explanation().options());
            }
        }, new StrictUserExplanation());
    }


}

