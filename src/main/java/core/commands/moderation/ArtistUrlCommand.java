package core.commands.moderation;

import core.Chuu;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistUrlParser;
import core.parsers.Parser;
import core.parsers.params.ArtistUrlParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.OptionalLong;

public class ArtistUrlCommand extends ConcurrentCommand<ArtistUrlParameters> {
    public ArtistUrlCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.ARTIST_IMAGES;
    }

    @Override
    public Parser<ArtistUrlParameters> initParser() {
        return new ArtistUrlParser(db);
    }

    @Override
    public String getDescription() {
        return "Changes artist image that is displayed on some bot functionalities";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("url");
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull ArtistUrlParameters params) throws LastFmException {
        LastFMData lastFMData = params.getLastFMData();
        if (lastFMData.getRole().equals(Role.IMAGE_BLOCKED)) {
            sendMessageQueue(e, "You don't have enough permissions to add an image!");
            return;
        }
        String urlParsed = params.getUrl();
        String artist = params.getArtist();
        try (InputStream in = new URL(urlParsed).openStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                parser.sendError(parser.getErrorMessage(2), e);
                return;
            }
            ScrobbledArtist scrobbledArtist = CommandUtil.onlyCorrection(db, artist, lastFM, !params.isNoredirect());
            OptionalLong persistedId = db.checkArtistUrlExists(scrobbledArtist.getArtistId(), urlParsed);
            OptionalLong queuedId = db.checkQueuedUrlExists(scrobbledArtist.getArtistId(), urlParsed);

            if (persistedId.isPresent()) {
                sendMessageQueue(e, "That image already existed for artist: %s.\nAdded a vote to that image instead".formatted(CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist())));
                db.castVote(persistedId.getAsLong(), e.getAuthor().getIdLong(), true);
                return;
            } else if (queuedId.isPresent()) {
                sendMessageQueue(e, "That image for **%s** is already on the review queue.".formatted(CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist())));
                return;
            }
            db.userInsertQueueUrl(urlParsed, scrobbledArtist.getArtistId(), e.getAuthor().getIdLong());
            sendMessageQueue(e, "Submitted an image for %s.\nIt will be reviewed by a bot moderator.".formatted(CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist())));

        } catch (IOException exception) {
            parser.sendError(parser.getErrorMessage(2), e);
            Chuu.getLogger().warn(exception.getMessage(), exception);
        }

    }

    @Override
    public String getName() {
        return "Artist Url ";
    }


}
