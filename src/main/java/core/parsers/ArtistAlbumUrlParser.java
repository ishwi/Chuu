package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.AlbumExplanation;
import core.parsers.explanation.UrlExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.params.ArtistAlbumUrlParameters;
import core.parsers.utils.OptionalEntity;
import core.parsers.utils.Optionals;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
    public ArtistAlbumUrlParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        User oneUser = InteractionAux.parseUser(e);
        LastFMData userName = findLastfmFromID(oneUser, ctx);
        InteractionAux.ArtistAlbum artistAlbum = InteractionAux.parseAlbum(e, () -> sendError(this.getErrorMessage(0), ctx));
        if (artistAlbum == null) {
            return null;
        }
        String url = InteractionAux.parseUrl(e);
        if (url == null) {
            sendError(getErrorMessage(0), ctx);
            return null;
        }
        if (!UrlParser.isValidURL(url)) {
            if (tumblr.matcher(url).matches()) {
                sendError(getErrorMessage(3), ctx);
                return null;

            }
        } else {
            sendError(getErrorMessage(1), ctx);
            return null;
        }
        return new ArtistAlbumUrlParameters(ctx, artistAlbum.artist(), artistAlbum.album(), userName, url);
    }

    @Override
    void setUpOptionals() {
        opts.add(Optionals.NOREDIRECT.opt);
    }

    @Override
    public ArtistAlbumUrlParameters parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException, LastFmException {

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
        if (url == null && e instanceof ContextMessageReceived mes) {
            if (mes.e().getMessage().getAttachments().isEmpty()) {
                sendError(getErrorMessage(1), e);
                return null;

            } else {
                Message.Attachment attachment = mes.e().getMessage().getAttachments().get(0);
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
    public List<Explanation> getUsages() {
        AlbumExplanation albumExplanation = new AlbumExplanation();
        return Stream.of(albumExplanation.artist(), albumExplanation.album(), new UrlExplanation()).map(InteractionAux::required).toList();
    }


}
