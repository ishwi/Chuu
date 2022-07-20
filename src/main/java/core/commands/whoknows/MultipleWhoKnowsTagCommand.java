package core.commands.whoknows;

import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.imagerenderer.ExetricWKMaker;
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
import java.util.List;
import java.util.Optional;
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
    WhoKnowsDisplayMode getWhoknowsMode(MultipleGenresParameters params) {
        LastFMData lastFMData = params.getLastFMData();
        if (lastFMData == null) {
            try {
                if (params.getE().isFromGuild())
                    return db.computeLastFmData(params.getE().getAuthor().getIdLong(), params.getE().getGuild().getIdLong()).getWhoKnowsMode();
                return WhoKnowsDisplayMode.IMAGE;
            } catch (InstanceNotFoundException exception) {
                return WhoKnowsDisplayMode.IMAGE;
            }
        } else {
            return lastFMData.getWhoKnowsMode();
        }
    }

    @Override
    BufferedImage doImage(MultipleGenresParameters ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        Context e = ap.getE();

        BufferedImage logo = null;
        ImageTitle title;
        if (e.isFromGuild()) {
            logo = CommandUtil.getLogo(db, e);
            title = new ImageTitle(e.getGuild().getName(), e.getGuild().getIconUrl());
        } else {
            title = new ImageTitle(e.getJDA().getSelfUser().getName(), e.getJDA().getSelfUser().getAvatarUrl());
        }
        handleWkMode(ap, wrapperReturnNowPlaying, WhoKnowsDisplayMode.IMAGE);
        List<String> urls = db.getTopInTag(ap.getGenres(), e.getGuild().getIdLong(), 100, ap.getMode()).stream().map(ScrobbledArtist::getUrl).filter(not(StringUtil::isBlank)).toList();
        BufferedImage thumb = ThumbsMaker.generate(urls);


        LastFMData data = obtainLastFmData(ap);
        BufferedImage image;
        if (data.getWkModes().contains(WKMode.BETA)) {
            image = ExetricWKMaker.generateWhoKnows(wrapperReturnNowPlaying, title.title(), title.logo(), logo, thumb);
        } else {
            image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, title.title(), logo, thumb);
        }
        sendImage(image, e);
        return logo;
    }

    @Override
    LastFMData obtainLastFmData(MultipleGenresParameters ap) {
        return ap.getLastFMData();
    }

    @Override
    public Optional<Rank<ReturnNowPlaying>> fetchNotInList(MultipleGenresParameters ap, WrapperReturnNowPlaying wr) {
        return Optional.empty();
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
    WrapperReturnNowPlaying generateWrapper(MultipleGenresParameters params, WhoKnowsDisplayMode whoKnowsDisplayMode) {
        Context e = params.getE();
        SearchMode mode = params.getMode();
//        CompletableFuture<Optional<ScrobbledArtist>> completableFuture = CompletableFuture.supplyAsync(() -> db.getTopInTag(params.getGenres(), e.getGuild().getIdLong(), mode));

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
