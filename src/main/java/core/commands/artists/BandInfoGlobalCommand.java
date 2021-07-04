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

public class BandInfoGlobalCommand extends BandInfoCommand {
    public BandInfoGlobalCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public String getDescription() {
        return "Like artist command but for all the users in the bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalartist", "ga");
    }

    @Override
    public String slashName() {
        return "artist";
    }

    @Override
    public String getName() {
        return "Specific Artist Overview in the bot";
    }

    @Override
    void bandLogic(ArtistParameters ap) {

        boolean b = ap.hasOptional("list");
        boolean b1 = ap.hasOptional("pie");
        int limit = b || b1 ? Integer.MAX_VALUE : 9;
        ScrobbledArtist who = ap.getScrobbledArtist();
        List<AlbumUserPlays> userTopArtistAlbums = db.getGlobalTopArtistAlbums(limit, who.getArtistId());
        Context e = ap.getE();
        long threshold = ap.getLastFMData().getArtistThreshold();

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
        long plays = db.getGlobalArtistPlays(who.getArtistId());
        doImage(ap, np, ai, Math.toIntExact(plays), logo, threshold);
    }

    @Override
    void doImage(ArtistParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai, int plays, BufferedImage logo, long threshold) {
        BufferedImage returnedImage = BandRendered
                .makeBandImage(np, ai, plays, logo, ap.getE().getJDA().getSelfUser().getName(), threshold);
        sendImage(returnedImage, ap.getE());
    }

    @Override
    void configEmbedBuilder(EmbedBuilder embedBuilder, ArtistParameters ap, ArtistAlbums ai) {
        embedBuilder.setTitle(ap.getE().getJDA().getSelfUser().getName() + "'s top " + CommandUtil.escapeMarkdown(ai.getArtist()) + " albums");

    }
}
