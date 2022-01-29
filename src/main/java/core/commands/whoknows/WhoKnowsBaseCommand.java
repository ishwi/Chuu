package core.commands.whoknows;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.WhoKnowsMaker;
import core.imagerenderer.util.pie.IPieableList;
import core.imagerenderer.util.pie.PieableListKnows;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.DaoParser;
import core.parsers.params.CommandParameters;
import core.parsers.utils.Optionals;
import dao.ServiceView;
import dao.entities.*;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.PieChart;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class WhoKnowsBaseCommand<T extends CommandParameters> extends ConcurrentCommand<T> {
    final DiscogsApi discogsApi;
    final Spotify spotify;
    private final IPieableList<ReturnNowPlaying, T> pie;

    public WhoKnowsBaseCommand(ServiceView dao, boolean isLongRunningCommand) {
        super(dao, isLongRunningCommand);
        if (this.parser instanceof DaoParser<?> p) {
            p.setExpensiveSearch(true);
            p.setAllowUnaothorizedUsers(true);
        }
        pie = new PieableListKnows<>(parser);
        parser.addOptional(Optionals.LIST.opt);
        this.respondInPrivate = false;
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
        order = 2;
    }

    public WhoKnowsBaseCommand(ServiceView dao) {
        this(dao, false);

    }

    public static WhoKnowsMode getEffectiveMode(WhoKnowsMode whoKnowsMode, CommandParameters chartParameters) {
        boolean pie = chartParameters.hasOptional("pie");
        boolean list = chartParameters.hasOptional("list");
        if ((whoKnowsMode.equals(WhoKnowsMode.LIST) && !list && !pie) || (!whoKnowsMode.equals(WhoKnowsMode.LIST) && list)) {
            return WhoKnowsMode.LIST;
        } else if (whoKnowsMode.equals(WhoKnowsMode.PIE) && !pie || !whoKnowsMode.equals(WhoKnowsMode.PIE) && pie) {
            return WhoKnowsMode.PIE;
        } else {
            return WhoKnowsMode.IMAGE;
        }
    }

    @Override
    public CommandCategory initCategory() {
        return CommandCategory.WHO_KNOWS;
    }

    @Override
    public void onCommand(Context e, @Nonnull T params) throws LastFmException {


        WhoKnowsMode whoknowsMode = getWhoknowsMode(params);
        WrapperReturnNowPlaying wrapperReturnNowPlaying = generateWrapper(params, whoknowsMode);
        if (wrapperReturnNowPlaying == null) {
            return;
        }
        wrapperReturnNowPlaying.setIndexes();
        generateWhoKnows(wrapperReturnNowPlaying, params, e.getAuthor().getIdLong(), whoknowsMode);

    }


    WhoKnowsMode getWhoknowsMode(T params) {
        return getEffectiveMode(obtainLastFmData(params).getWhoKnowsMode(), params);
    }

    abstract WrapperReturnNowPlaying generateWrapper(T params, WhoKnowsMode whoKnowsMode) throws LastFmException;

    public void generateWhoKnows(WrapperReturnNowPlaying wrapperReturnNowPlaying, T ap, long author, WhoKnowsMode effectiveMode) {
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x ->
                        x.setGenerateString(supplierGenerator(ap, x))
                );
        switch (effectiveMode) {

            case IMAGE, PIE -> {
                if (effectiveMode.equals(WhoKnowsMode.IMAGE)) {
                    doImage(ap, wrapperReturnNowPlaying);
                } else {
                    doPie(ap, wrapperReturnNowPlaying);
                }
            }
            case LIST -> doList(ap, wrapperReturnNowPlaying);
        }
    }

    @NotNull
    Supplier<String> supplierGenerator(T ap, ReturnNowPlaying x) {
        return () -> {
            String userString = getUserString(ap.getE(), x.getDiscordId());
            x.setDiscordName(userString);
            return x.getIndex() + 1 + ". " +
                    "**[" + LinkUtils.cleanMarkdownCharacter(userString) + "](" +
                    PrivacyUtils.getUrlTitle(x) +
                    ")** - " +
                    x.getPlayNumber() + " plays\n";
        };
    }

    protected String getImageTitle(Context e, T params) {
        String title;
        if (e.isFromGuild()) {
            title = e.getGuild().getName();
        } else {
            title = e.getJDA().getSelfUser().getName();
        }
        return title;
    }

    BufferedImage doImage(T ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        Context e = ap.getE();

        BufferedImage logo = null;
        String title = getImageTitle(e, ap);
        if (e.isFromGuild()) {
            logo = CommandUtil.getLogo(db, e);
        }
        handleWkMode(ap, wrapperReturnNowPlaying);
        BufferedImage image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, EnumSet.allOf(WKMode.class), title, logo, ap.getE().getAuthor().getIdLong());
        sendImage(image, e);

        return logo;
    }

    void handleWkMode(T ap, WrapperReturnNowPlaying wr) {
        List<ReturnNowPlaying> rnp = wr.getReturnNowPlayings();
        LastFMData data = obtainLastFmData(ap);
        if (rnp.stream().limit(10).noneMatch(np -> np.getDiscordId() == data.getDiscordId())) {
            if (rnp.size() >= 10) {
                Optional<Rank<ReturnNowPlaying>> userOpt = fetchNotInList(ap, wr);
                if (userOpt.isPresent()) {
                    Rank<ReturnNowPlaying> userPos = userOpt.get();
                    ReturnNowPlaying rn = userPos.entity();
                    rn.setIndex(userPos.rank());
                    rnp.set(9, rn);
                    if (rn.getGenerateString() == null) {
                        rn.setGenerateString(supplierGenerator(ap, rn));
                    }
                }
            }
        }

    }

    abstract LastFMData obtainLastFmData(T ap);


    public abstract Optional<Rank<ReturnNowPlaying>> fetchNotInList(T ap, WrapperReturnNowPlaying wr);

    void doList(T ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {

        Context e = ap.getE();

        String usable;
        if (e.isFromGuild()) {
            usable = CommandUtil.escapeMarkdown(e.getGuild().getName());
        } else {
            usable = e.getJDA().getSelfUser().getName();
        }

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(ap.getE()).setTitle(getTitle(ap, usable)).
                setThumbnail(CommandUtil.noImageUrl(wrapperReturnNowPlaying.getUrl()));

        new PaginatorBuilder<>(e, embedBuilder, wrapperReturnNowPlaying.getReturnNowPlayings()).unnumered().build().queue();

    }

    void doPie(T ap, WrapperReturnNowPlaying returnNowPlaying) {
        PieChart pieChart = this.pie.doPie(ap, returnNowPlaying.getReturnNowPlayings());
        String usable;
        Context e = ap.getE();
        if (e.isFromGuild()) {
            usable = CommandUtil.escapeMarkdown(e.getGuild().getName());
        } else {
            usable = e.getJDA().getSelfUser().getName();
        }
        pieChart.setTitle(getTitle(ap, usable));
        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        GraphicUtils.setQuality(g);

        pieChart.paint(g, 1000, 750);
        BufferedImage image = GraphicUtils.getImage(returnNowPlaying.getUrl());
        if (image != null) {
            BufferedImage backgroundImage = Scalr.resize(image, 150);
            g.drawImage(backgroundImage, 10, 750 - 10 - backgroundImage.getHeight(), null);
        }
        sendImage(bufferedImage, e);
    }

    public abstract String getTitle(T params, String baseTitle);
}
