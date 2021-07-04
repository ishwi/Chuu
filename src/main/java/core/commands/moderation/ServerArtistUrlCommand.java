//package core.commands.moderation;
//
//import core.Chuu;
//import core.commands.abstracts.ConcurrentCommand;
//import core.commands.utils.CommandCategory;
//import core.commands.utils.CommandUtil;
//import core.exceptions.LastFmException;
//import core.parsers.ArtistUrlParser;
//import core.parsers.Parser;
//import core.parsers.params.ArtistUrlParameters;
//import dao.ChuuService;
//import dao.entities.LastFMData;
//import dao.entities.ScrobbledArtist;
//import net.dv8tion.jda.api.Permission;
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//
//import javax.imageio.ImageIO;
//import javax.validation.constraints.NotNull;
//import java.awt.image.BufferedImage;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.util.Arrays;
//import java.util.List;
//import java.util.OptionalLong;
//
//public class ServerArtistUrlCommand extends ConcurrentCommand<ArtistUrlParameters> {
//    public ServerArtistUrlCommand(ServiceView dao) {
//        super(dao);
//    }
//
//
//    @Override
//    protected CommandCategory initCategory() {
//        return CommandCategory.ARTIST_IMAGES;
//    }
//
//    @Override
//    public Parser<ArtistUrlParameters> initParser() {
//        return new ArtistUrlParser(dao);
//    }
//
//    @Override
//    public String getDescription() {
//        return "Sets a image for an artist in your server so it's independent from voting";
//    }
//
//    @Override
//    public List<String> getAliases() {
//        return Arrays.asList("serverurl", "surl");
//    }
//
//    @Override
//    protected void onCommand(Context e, @NotNull ArtistUrlParameters params) throws LastFmException {
//        if (e.getMember() == null || !e.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
//            sendMessageQueue(e, "Only server mods can use this command");
//            return;
//        }
//        LastFMData lastFMData = params.getLastFMData();
//        String urlParsed = params.getUrl();
//        String artist = params.getArtist();
//        try (InputStream in = new URL(urlParsed).openStream()) {
//            BufferedImage image = ImageIO.read(in);
//            if (image == null) {
//                parser.sendError(parser.getErrorMessage(2), e);
//                return;
//            }
//            ScrobbledArtist scrobbledArtist = new ArtistValidator(dao,lastFM,e).validate(artist,false,!params.isNoredirect());
//            OptionalLong persistedId = dao.checkArtistUrlExists(scrobbledArtist.getArtistId(), urlParsed);
//            OptionalLong queuedId = dao.checkQueuedUrlExists(scrobbledArtist.getArtistId(), urlParsed);
//
//            if (persistedId.isPresent()) {
//                dao.insertServerCustomUrl(persistedId.getAsLong(), params.getE().getGuild().getIdLong(), scrobbledArtist.getArtistId());
//                sendMessageQueue(e, "Set this as the server image for " + CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist()) + ".");
//                return;
//            } else if (queuedId.isPresent()) {
//                sendMessageQueue(e, "That image for **" + CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist()) + "** is already on the review queue.");
//                return;
//            }
//            dao.userInsertQueueUrlForServer(urlParsed, scrobbledArtist.getArtistId(), e.getAuthor().getIdLong(),e.getGuild().getIdLong());
//            sendMessageQueue(e, "Submitted a server image for " + CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist()) + ".\nIt will be reviewed by a bot moderator.\nIf it gets accepted it will be set as the default image.");
//
//        } catch (IOException exception) {
//            parser.sendError(parser.getErrorMessage(2), e);
//            Chuu.getLogger().warn(exception.getMessage(), exception);
//        }
//
//    }
//
//    @Override
//    public String getName() {
//        return "Server artist image";
//    }
//
//
//}
