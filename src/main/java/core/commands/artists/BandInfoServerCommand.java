package core.commands.artists;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.BandRendered;
import core.parsers.params.ArtistParameters;
import dao.ServiceView;
import dao.entities.AlbumUserPlays;
import dao.entities.ArtistAlbums;
import dao.entities.ScrobbledArtist;
import dao.entities.WrapperReturnNowPlaying;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.image.BufferedImage;
import java.util.List;

public class BandInfoServerCommand extends BandInfoCommand {
    public BandInfoServerCommand(ServiceView dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public String getDescription() {
        return "Like artist command but for all the users in the server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("serverartist", "sa");
    }

    @Override
    public String slashName() {
        return "artist";
    }

    @Override
    public String getName() {
        return "Specific Artist Overview in a Server";
    }

    @Override
    protected void bandLogic(ArtistParameters ap) {

        boolean b = ap.hasOptional("list");
        boolean b1 = ap.hasOptional("pie");
        int limit = b || b1 ? Integer.MAX_VALUE : 9;
        ScrobbledArtist who = ap.getScrobbledArtist();
        long threshold = ap.getLastFMData().getArtistThreshold();

        List<AlbumUserPlays> userTopArtistAlbums = db.getServerTopArtistAlbums(limit, who.getArtistId(), ap.getE().getGuild().getIdLong());
        Context e = ap.getE();
        userTopArtistAlbums.forEach(t -> t.setAlbumUrl(Chuu.getCoverService().getCover(t.getArtist(), t.getAlbum(), t.getAlbumUrl(), e)));

        ArtistAlbums ai = new ArtistAlbums(who.getArtist(), userTopArtistAlbums);

        if (b || !e.isFromGuild()) {
            doList(ap, ai);
            return;
        }
        WrapperReturnNowPlaying np = db.whoKnows(who.getArtistId(), e.getGuild().getIdLong(), 5);
        np.getReturnNowPlayings().forEach(element ->
                element.setDiscordName(CommandUtil.getUserInfoUnescaped(e, element.getDiscordId()).username())
        );
        BufferedImage logo = CommandUtil.getLogo(db, e);
        if (b1) {
            doPie(ap, np, ai, logo);
            return;
        }
        long plays = db.getServerArtistPlays(ap.getE().getGuild().getIdLong(), who.getArtistId());
        doImage(ap, np, ai, Math.toIntExact(plays), logo, threshold);
    }


    @Override
    protected void doImage(ArtistParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai, int plays, BufferedImage logo, long threshold) {
        np.setIndexes();
        BufferedImage returnedImage = BandRendered
                .makeBandImage(np, ai, plays, logo, ap.getE().getGuild().getName(), threshold);
        sendImage(returnedImage, ap.getE());
    }

    @Override
    protected void configEmbedBuilder(EmbedBuilder embedBuilder, ArtistParameters ap, ArtistAlbums ai) {
        embedBuilder.setTitle(ap.getE().getGuild().getName() + "'s top " + CommandUtil.escapeMarkdown(ai.getArtist()) + " albums");

    }
}
