package core.commands.whoknows;

import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.ExetricWKMaker;
import core.imagerenderer.ThumbsMaker;
import core.imagerenderer.WhoKnowsMaker;
import core.parsers.GenreParser;
import core.parsers.Parser;
import core.parsers.params.GenreParameters;
import core.util.ServiceView;
import dao.entities.*;
import org.jsoup.internal.StringUtil;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

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
    public String slashName() {
        return "who-knows";
    }

    @Override
    public CommandCategory initCategory() {
        return CommandCategory.GENRES;
    }

    @Override
    public void generateWhoKnows(WrapperReturnNowPlaying wrapperReturnNowPlaying, GenreParameters ap, long author, WhoKnowsDisplayMode effectiveMode) {
        super.generateWhoKnows(wrapperReturnNowPlaying, ap, author, effectiveMode);
    }

    @Override
    BufferedImage doImage(GenreParameters ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        Context e = ap.getE();

        BufferedImage logo = null;
        ImageTitle title;
        if (e.isFromGuild()) {
            logo = CommandUtil.getLogo(db, e);
            title = new ImageTitle(e.getGuild().getName(), e.getGuild().getIconUrl());
        } else {
            title = new ImageTitle(e.getJDA().getSelfUser().getName(), e.getJDA().getSelfUser().getAvatarUrl());
        }
        List<String> urls = db.getTopInTag(ap.getGenre(), e.getGuild().getIdLong(), 100).stream().map(ScrobbledArtist::getUrl).filter(not(StringUtil::isBlank)).toList();
        BufferedImage thumb = ThumbsMaker.generate(urls);
        handleWkMode(ap, wrapperReturnNowPlaying, WhoKnowsDisplayMode.IMAGE);
        LastFMData data = obtainLastFmData(ap);
        BufferedImage image;
        if (data.getWkModes().contains(WKMode.BETA)) {
            image = ExetricWKMaker.generateWhoKnows(wrapperReturnNowPlaying, title.title(), title.logo(), logo, thumb);
        } else {
            image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, title.title(), logo, thumb);
        }
        sendImage(image, e);
        return image;
    }

    @Override
    LastFMData obtainLastFmData(GenreParameters ap) {
        return ap.getLastFMData();
    }

    @Override
    public Optional<Rank<ReturnNowPlaying>> fetchNotInList(GenreParameters ap, WrapperReturnNowPlaying wr) {
        return Optional.empty();
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(GenreParameters params, WhoKnowsDisplayMode whoKnowsDisplayMode) {
        Context e = params.getE();
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                whoKnowsDisplayMode.equals(WhoKnowsDisplayMode.IMAGE) ?
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
