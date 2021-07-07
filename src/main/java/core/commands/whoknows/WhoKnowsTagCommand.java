package core.commands.whoknows;

import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.ThumbsMaker;
import core.imagerenderer.WhoKnowsMaker;
import core.parsers.GenreParser;
import core.parsers.Parser;
import core.parsers.params.GenreParameters;
import dao.ServiceView;
import dao.entities.ScrobbledArtist;
import dao.entities.WKMode;
import dao.entities.WhoKnowsMode;
import dao.entities.WrapperReturnNowPlaying;
import org.jsoup.internal.StringUtil;

import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.List;

import static core.commands.whoknows.MultipleWhoKnowsTagCommand.formatTag;
import static java.util.function.Predicate.not;

public class WhoKnowsTagCommand extends WhoKnowsBaseCommand<GenreParameters> {
    public WhoKnowsTagCommand(ServiceView dao) {
        super(dao);
    }

    public WhoKnowsTagCommand(ServiceView dao, boolean isLongRunningCommand) {
        super(dao, true);
    }

    @Override
    WhoKnowsMode getWhoknowsMode(GenreParameters params) {
        return getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);
    }

    @Override
    public String slashName() {
        return "who-knows";
    }

    @Override
    public CommandCategory initCategory() {
        return CommandCategory.GENRES;
    }

    @Override
    public void generateWhoKnows(WrapperReturnNowPlaying wrapperReturnNowPlaying, GenreParameters ap, long author, WhoKnowsMode effectiveMode) {
        super.generateWhoKnows(wrapperReturnNowPlaying, ap, author, effectiveMode);
    }

    @Override
    BufferedImage doImage(GenreParameters ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        Context e = ap.getE();

        BufferedImage logo = null;
        String title;
        if (e.isFromGuild()) {
            logo = CommandUtil.getLogo(db, e);
            title = e.getGuild().getName();
        } else {
            title = e.getJDA().getSelfUser().getName();
        }
        List<String> urls = db.getTopInTag(ap.getGenre(), e.getGuild().getIdLong(), 100).stream().map(ScrobbledArtist::getUrl).filter(not(StringUtil::isBlank)).toList();
        BufferedImage thumb = ThumbsMaker.generate(urls);

        BufferedImage image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, EnumSet.allOf(WKMode.class), title, logo, ap.getE().getAuthor().getIdLong(), thumb);
        sendImage(image, e);
        return logo;
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(GenreParameters params, WhoKnowsMode whoKnowsMode) {
        Context e = params.getE();
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                whoKnowsMode.equals(WhoKnowsMode.IMAGE) ?
                this.db.whoKnowsGenre(params.getGenre(), e.getGuild().getIdLong()) :
                this.db.whoKnowsGenre(params.getGenre(), e.getGuild().getIdLong(), Integer.MAX_VALUE);
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(e, "No one knows " + CommandUtil.escapeMarkdown(params.getGenre()));
            return null;
        }
        return formatTag(e, wrapperReturnNowPlaying);
    }

    @Override
    public String getTitle(GenreParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.escapeMarkdown(params.getGenre()) + " in " + baseTitle + "?";

    }


    @Override
    public Parser<GenreParameters> initParser() {
        return new GenreParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Who knows a given tag on the bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("whoknowsgenre", "whoknowstag", "wktag", "wkg", "wg");
    }

    @Override
    public String getName() {
        return "Who knows Genre";
    }
}
