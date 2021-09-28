package core.commands.whoknows;

import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.ThumbsMaker;
import core.imagerenderer.WhoKnowsMaker;
import core.parsers.MultipleGenresParser;
import core.parsers.Parser;
import core.parsers.params.MultipleGenresParameters;
import dao.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import org.apache.commons.text.WordUtils;
import org.jsoup.internal.StringUtil;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class MultipleWhoKnowsTagCommand extends WhoKnowsBaseCommand<MultipleGenresParameters> {
    public MultipleWhoKnowsTagCommand(ServiceView dao) {
        super(dao, true);
    }

    @Nonnull
    static WrapperReturnNowPlaying formatTag(Context e, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x -> x.setDiscordName(CommandUtil.getUserInfoUnescaped(e, x.getDiscordId()).username()));
        return wrapperReturnNowPlaying;
    }

    @Override
    WhoKnowsMode getWhoknowsMode(MultipleGenresParameters params) {
        LastFMData lastFMData = params.getLastFMData();
        if (lastFMData == null) {
            try {
                if (params.getE().isFromGuild())
                    return db.computeLastFmData(params.getE().getAuthor().getIdLong(), params.getE().getGuild().getIdLong()).getWhoKnowsMode();
                return WhoKnowsMode.IMAGE;
            } catch (InstanceNotFoundException exception) {
                return WhoKnowsMode.IMAGE;
            }
        } else {
            return lastFMData.getWhoKnowsMode();
        }
    }

    @Override
    BufferedImage doImage(MultipleGenresParameters ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        Context e = ap.getE();

        BufferedImage logo = null;
        String title;
        if (e.isFromGuild()) {
            logo = CommandUtil.getLogo(db, e);
            title = e.getGuild().getName();
        } else {
            title = e.getJDA().getSelfUser().getName();
        }
        List<String> urls = db.getTopInTag(ap.getGenres(), e.getGuild().getIdLong(), 100, ap.getMode()).stream().map(ScrobbledArtist::getUrl).filter(not(StringUtil::isBlank)).toList();
        BufferedImage thumb = ThumbsMaker.generate(urls);

        BufferedImage image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, EnumSet.allOf(WKMode.class), title, logo, ap.getE().getAuthor().getIdLong(), thumb);
        sendImage(image, e);
        return logo;
    }

    @Override
    public CommandCategory initCategory() {
        return CommandCategory.GENRES;
    }

    @Override
    public String slashName() {
        return "multi-who-knows";
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(MultipleGenresParameters params, WhoKnowsMode whoKnowsMode) {
        Context e = params.getE();
        SearchMode mode = params.getMode();
        CompletableFuture<Optional<ScrobbledArtist>> completableFuture = CompletableFuture.supplyAsync(() -> db.getTopInTag(params.getGenres(), e.getGuild().getIdLong(), mode));

        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                this.db.getWhoKnowsTagSet(params.getGenres(), e.getGuild().getIdLong(), Integer.MAX_VALUE, null, mode);
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(e, "No one knows " + CommandUtil.escapeMarkdown(params.getGenres().stream().map(WordUtils::capitalizeFully).collect(Collectors.joining(","))));
            return null;
        }

        return formatTag(e, wrapperReturnNowPlaying);
    }

    @Override
    public String getTitle(MultipleGenresParameters params, String baseTitle) {
        return "mwkt";
    }


    @Override
    public Parser<MultipleGenresParameters> initParser() {
        return new MultipleGenresParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Returns a list of all the artist that match multiple tags";
    }

    public List<String> getAliases() {
        return List.of("multiplewhoknowsgenre", "multiwhoknowstag", "multiwktag", "multiwkg", "mwkg", "mwkt", "mwg");
    }


    @Override
    public String getName() {
        return "Multiple Who Knows Tag";
    }
}
