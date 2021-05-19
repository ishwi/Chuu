package core.commands.moderation;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.ArtistAlbumUrlParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumUrlParameters;
import core.services.AlbumValidator;
import core.services.CoverService;
import dao.ServiceView;
import dao.entities.CoverItem;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.entities.ScrobbledAlbum;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class BanCoverCommand extends ConcurrentCommand<ArtistAlbumUrlParameters> {
    public BanCoverCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<ArtistAlbumUrlParameters> initParser() {
        return new ArtistAlbumUrlParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Gives an alt cover for a nsfw cover";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("altcover", "altc");
    }

    @Override
    public String getName() {
        return "Alt cover";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistAlbumUrlParameters params) throws LastFmException {
        LastFMData lastFMData = params.getLastFMData();
        if (lastFMData.getRole() != Role.ADMIN) {
            sendMessageQueue(e, "Not enough chuu perms to do this");
            return;
        }
        CoverService coverService = Chuu.getCoverService();
        ScrobbledAlbum scrobbledAlbum = new AlbumValidator(db, lastFM).validate(params.getArtist(), params.getAlbum());
        List<String> covers = coverService.getCovers(scrobbledAlbum.getAlbumId());


        try (InputStream in = new URL(params.getUrl()).openStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                parser.sendError(parser.getErrorMessage(2), e);
                return;
            }

            if (covers.contains(params.getUrl())) {
                sendMessageQueue(e, "This cover was already introduced for this artist");
                return;
            }
            coverService.addCover(new CoverItem(params.getAlbum(), params.getArtist(), scrobbledAlbum.getAlbumId()), params.getUrl());
            sendMessageQueue(e, "Added <%s> as an alt cover for **%s - %s**".formatted(params.getUrl(), params.getArtist(), params.getAlbum()));

        } catch (IOException exception) {
            parser.sendError(parser.getErrorMessage(2), e);
            Chuu.getLogger().warn(exception.getMessage(), exception);
        }


    }
}
