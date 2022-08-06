package core.commands.artists;

import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.BandRendered;
import core.parsers.params.ArtistParameters;
import core.util.ServiceView;
import dao.entities.ArtistAlbums;
import dao.entities.ScrobbledArtist;
import dao.entities.WrapperReturnNowPlaying;
import dao.exceptions.ChuuServiceException;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
    protected void bandLogic(ArtistParameters ap) {

        boolean b = ap.hasOptional("list");
        boolean b1 = ap.hasOptional("pie");
        int limit = b || b1 ? Integer.MAX_VALUE : 9;
        ScrobbledArtist who = ap.getScrobbledArtist();
        Context e = ap.getE();

        try (var scope = new BandScope()) {
            scope.fork(() -> new Albums(db.getGlobalTopArtistAlbums(limit, who.getArtistId())));
            if (e.isFromGuild()) {
                scope.fork(() -> new WK(db.whoKnows(who.getArtistId(), e.getGuild().getIdLong(), 5)));
            } else {
                scope.fork(() -> new WK(db.globalWhoKnows(who.getArtistId(), 5, false, e.getAuthor().getIdLong(), false)));
            }
            scope.fork(() -> new AP(db.getGlobalArtistPlays(who.getArtistId())));
            scope.joinUntil(Instant.now().plus(10, ChronoUnit.SECONDS));
            BandResult sr = scope.result();
            doDisplay(sr, ap);
        } catch (StructuredNotHandledException | InterruptedException | TimeoutException ex) {
            throw new ChuuServiceException(ex);
        }
    }

    @Override
    protected void doImage(ArtistParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai,
                           long plays, long threshold) {
        np.setIndexes();
        BufferedImage returnedImage = BandRendered
                .makeBandImage(np, ai, plays, ap.getE().getJDA().getSelfUser().getName(), threshold);
        sendImage(returnedImage, ap.getE());
    }

    @Override
    protected void configEmbedBuilder(EmbedBuilder embedBuilder, ArtistParameters ap, ArtistAlbums ai) {
        embedBuilder.setTitle(ap.getE().getJDA().getSelfUser().getName() + "'s top " + CommandUtil.escapeMarkdown(ai.getArtist()) + " albums");

    }
}
