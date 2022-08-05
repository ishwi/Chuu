package core.commands.moderation;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.Parser;
import core.parsers.UrlParser;
import core.parsers.params.UrlParameters;
import core.util.ServiceView;
import org.imgscalr.Scalr;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class AdministrativeCommand extends ConcurrentCommand<UrlParameters> {


    public AdministrativeCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<UrlParameters> initParser() {
        return new UrlParser();
    }


    @Override
    public String getDescription() {
        return "Adds a logo that will be displayed on some bot functionalities";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("logo");
    }

    @Override
    public String getName() {
        return "Logo";
    }

    @Override
    public void onCommand(Context e, @Nonnull UrlParameters urlParameters) {
        String url = urlParameters.getUrl();
        if (url.length() == 0) {
            db.removeLogo(e.getGuild().getIdLong());
            sendMessageQueue(e, "Removed logo from the server");
        } else {

            try (var in = new BufferedInputStream(new URL(url).openStream())) {
                BufferedImage image = ImageIO.read(in);
                if (image == null) {
                    sendMessageQueue(e, "Couldn't get an image from the supplied link");
                    return;
                }
                image = Scalr.resize(image, Scalr.Method.QUALITY, 75, Scalr.OP_ANTIALIAS);

                db.addLogo(e.getGuild().getIdLong(), image);
                sendMessageQueue(e, "Logo updated");
            } catch (IOException exception) {
                Chuu.getLogger().warn(exception.getMessage(), exception);
                sendMessageQueue(e, "Something happened while processing the image");
            }

        }
    }
}
