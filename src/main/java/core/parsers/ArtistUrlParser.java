package core.parsers;

import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.parsers.explanation.ArtistExplanation;
import core.parsers.explanation.UrlExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.ArtistUrlParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.regex.Pattern;

public class ArtistUrlParser extends DaoParser<ArtistUrlParameters> {

    private static final Pattern tumblr = Pattern.compile("https://(\\d+)\\.media\\.tumblr\\.com/.*");

    public ArtistUrlParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    public void setUpErrorMessages() {
        errorMessages.put(0, "You need to specify the artist and the url!!");
        errorMessages.put(1, "You didn't specify a valid URL");
        errorMessages.put(2, "Couldn't get an Image from link supplied");
        errorMessages.put(3, "Tumblr images in that format cannot be read from a bot, you can try to copy and paste the image instead of the link");
    }

    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("noredirect", "not change the artist name for a correction automatically"));
    }

    @Override
    public ArtistUrlParameters parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {

        boolean noUrl = true;

        String artist;
        String url = null;
        if (subMessage.length >= 1) {
            StringBuilder a = new StringBuilder();
            for (String s : subMessage) {
                if (noUrl && UrlParser.isValidURL(s)) {
                    if (tumblr.matcher(s).matches()) {
                        sendError(getErrorMessage(3), e);
                        return null;
                    }
                    noUrl = false;
                    url = s;
                    continue;
                }
                a.append(s).append(" ");
            }
            artist = a.toString().trim();

        } else {
            sendError(getErrorMessage(0), e);
            return null;
        }
        if (url == null && e instanceof ContextMessageReceived mes) {
            if (mes.e().getMessage().getAttachments().isEmpty()) {
                sendError(getErrorMessage(1), e);
                return null;

            } else {
                Message.Attachment attachment = mes.e().getMessage().getAttachments().get(0);
                url = attachment.getUrl();
            }
        }
        if (artist.isBlank()) {
            sendError(getErrorMessage(0), e);
            return null;
        }

        return new ArtistUrlParameters(e, artist, findLastfmFromID(e.getAuthor(), e), url);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new ArtistExplanation(), new UrlExplanation());
    }


}
