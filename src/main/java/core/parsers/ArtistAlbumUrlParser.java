package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.params.ArtistAlbumUrlParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Pattern;

public class ArtistAlbumUrlParser extends DaoParser<ArtistAlbumUrlParameters> {

    private static final Pattern tumblr = Pattern.compile("https://(\\d+)\\.media\\.tumblr\\.com/.*");
    private final ArtistAlbumParser parser;

    public ArtistAlbumUrlParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... opts) {
        super(dao, opts);
        parser = new ArtistAlbumParser(dao, lastFM);
    }

    @Override
    public void setUpErrorMessages() {
        errorMessages.put(0, "You need to specify the artist,album and the url!!");
        errorMessages.put(1, "You didn't specify a valid URL");
        errorMessages.put(2, "Couldn't get an Image from link supplied");
        errorMessages.put(3, "Tumblr images in that format cannot be read from a bot, you can try to copy and paste the image instead of the link");
    }

    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("noredirect", "not change the artist name for a correction automatically"));
    }

    @Override
    public ArtistAlbumUrlParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException, LastFmException {

        boolean noUrl = true;

        String remaining;
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
            remaining = a.toString().trim();

        } else {
            sendError(getErrorMessage(0), e);
            return null;
        }
        if (url == null) {
            if (e.getMessage().getAttachments().isEmpty()) {
                sendError(getErrorMessage(1), e);
                return null;

            } else {
                Message.Attachment attachment = e.getMessage().getAttachments().get(0);
                url = attachment.getUrl();
            }
        }
        if (remaining.isBlank()) {
            sendError(getErrorMessage(0), e);
            return null;
        }
        ArtistAlbumParameters artistAlbumParameters = parser.parseLogic(e, remaining.split("\\s+"));

        return new ArtistAlbumUrlParameters(e, artistAlbumParameters.getArtist(), artistAlbumParameters.getAlbum(), findLastfmFromID(e.getAuthor(), e), url);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *artist - album url***\n";

    }


}
