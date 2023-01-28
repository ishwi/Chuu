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
import core.imagerenderer.ExetricWKMaker;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.WhoKnowsMaker;
import core.imagerenderer.util.pie.IPieableList;
import core.imagerenderer.util.pie.PieableListKnows;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.DaoParser;
import core.parsers.params.CommandParameters;
import core.parsers.utils.Optionals;
import core.util.ServiceView;
import dao.entities.*;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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

    public static WhoKnowsDisplayMode getEffectiveMode(WhoKnowsDisplayMode whoKnowsDisplayMode, CommandParameters chartParameters) {
        boolean pie = chartParameters.hasOptional("pie");
        boolean list = chartParameters.hasOptional("list");
        if ((whoKnowsDisplayMode.equals(WhoKnowsDisplayMode.LIST) && !list && !pie) || (!whoKnowsDisplayMode.equals(WhoKnowsDisplayMode.LIST) && list)) {
            return WhoKnowsDisplayMode.LIST;
        } else if (whoKnowsDisplayMode.equals(WhoKnowsDisplayMode.PIE) && !pie || !whoKnowsDisplayMode.equals(WhoKnowsDisplayMode.PIE) && pie) {
            return WhoKnowsDisplayMode.PIE;
        } else {
            return WhoKnowsDisplayMode.IMAGE;
        }
    }

    @Override
    public CommandCategory initCategory() {
        return CommandCategory.WHO_KNOWS;
    }

    @Override
    public void onCommand(Context e, @NotNull T params) throws LastFmException {


        WhoKnowsDisplayMode whoknowsDisplayMode = getWhoknowsMode(params);
        WrapperReturnNowPlaying wrapperReturnNowPlaying = generateWrapper(params, whoknowsDisplayMode);
        if (wrapperReturnNowPlaying == null) {
            return;
        }
        wrapperReturnNowPlaying.setIndexes();
        generateWhoKnows(wrapperReturnNowPlaying, params, e.getAuthor().getIdLong(), whoknowsDisplayMode);

    }


    WhoKnowsDisplayMode getWhoknowsMode(T params) {
        return getEffectiveMode(obtainLastFmData(params).getWhoKnowsMode(), params);
    }

    abstract WrapperReturnNowPlaying generateWrapper(T params, WhoKnowsDisplayMode whoKnowsDisplayMode) throws LastFmException;

    public void generateWhoKnows(WrapperReturnNowPlaying wrapperReturnNowPlaying, T ap, long author, WhoKnowsDisplayMode effectiveMode) {
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x ->
                        x.setGenerateString(supplierGenerator(ap, x))
                );
        switch (effectiveMode) {

            case IMAGE, PIE -> {
                if (effectiveMode.equals(WhoKnowsDisplayMode.IMAGE)) {
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

    protected ImageTitle getImageTitle(Context e, T params) {
        if (e.isFromGuild()) {
            return new ImageTitle(e.getGuild().getName(), e.getGuild().getIconUrl());
        } else {
            return new ImageTitle(e.getJDA().getSelfUser().getName(), e.getJDA().getSelfUser().getAvatarUrl());
        }
    }

    BufferedImage doImage(T ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        Context e = ap.getE();

        BufferedImage logo = null;
        ImageTitle title = getImageTitle(e, ap);
        if (e.isFromGuild()) {
            logo = CommandUtil.getLogo(db, e);
        }
        handleWkMode(ap, wrapperReturnNowPlaying, WhoKnowsDisplayMode.IMAGE);
        LastFMData data = obtainLastFmData(ap);
        BufferedImage image;
        if (data.getWkModes().contains(WKMode.BETA)) {
            image = ExetricWKMaker.generateWhoKnows(wrapperReturnNowPlaying, title.title, title.logo, logo);
        } else {
            image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, title.title, logo);
        }
        sendImage(image, e);

        return logo;
    }


    Optional<ReturnNowPlaying> handleWkMode(T ap, WrapperReturnNowPlaying wr, WhoKnowsDisplayMode mode) {
        List<ReturnNowPlaying> rnp = wr.getReturnNowPlayings();
        LastFMData data = obtainLastFmData(ap);
        if (data.getWkModes().contains(WKMode.OWN_RANK)) {
            if (rnp.stream().limit(10).noneMatch(np -> np.getDiscordId() == data.getDiscordId())) {
                if (mode == WhoKnowsDisplayMode.LIST) {
                    boolean found = false;
                    int j = 0;
                    for (ReturnNowPlaying returnNowPlaying : rnp) {
                        if (returnNowPlaying.getDiscordId() == data.getDiscordId()) {
                            found = true;
                            break;
                        }
                        j++;
                    }
                    if (found) {
                        return Optional.of(rnp.get(j));
                    }
                } else {
                    if (rnp.size() >= 10) {
                        Optional<Rank<ReturnNowPlaying>> userOpt = fetchNotInList(ap, wr);
                        if (userOpt.isPresent()) {
                            Rank<ReturnNowPlaying> userPos = userOpt.get();
                            ReturnNowPlaying rn = userPos.entity();
                            rn.setIndex(userPos.rank());
                            List<ReturnNowPlaying> copy = new ArrayList<>(rnp);
                            copy.set(9, rn);
                            wr.setReturnNowPlayings(copy);

                            if (rn.getGenerateString() == null) {
                                rn.setGenerateString(supplierGenerator(ap, rn));
                            }
                        }
                        return userOpt.map(Rank::entity);
                    }
                }
            }
        }
        return Optional.empty();
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
        Optional<ReturnNowPlaying> rnp = handleWkMode(ap, wrapperReturnNowPlaying, WhoKnowsDisplayMode.LIST);

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(ap.getE()).setTitle(getTitle(ap, usable)).
                setThumbnail(CommandUtil.noImageUrl(wrapperReturnNowPlaying.getUrl()));

        PaginatorBuilder<ReturnNowPlaying> pb = new PaginatorBuilder<>(e, embedBuilder, wrapperReturnNowPlaying.getReturnNowPlayings()).mapper(ReturnNowPlaying::toDisplay).unnumered();
        rnp.ifPresent(returnNowPlaying -> pb.extraText(i -> returnNowPlaying.toDisplay()));
        pb.build().queue();

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

    public record ImageTitle(String title, String logo) {
    }
}
