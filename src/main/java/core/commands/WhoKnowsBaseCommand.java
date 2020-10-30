package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.WhoKnowsMaker;
import core.imagerenderer.util.IPieableList;
import core.imagerenderer.util.PieableListKnows;
import core.otherlisteners.Reactionary;
import core.parsers.DaoParser;
import core.parsers.OptionalEntity;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.ReturnNowPlaying;
import dao.entities.WhoKnowsMode;
import dao.entities.WrapperReturnNowPlaying;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.imgscalr.Scalr;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class WhoKnowsBaseCommand<T extends CommandParameters> extends ConcurrentCommand<T> {
    final DiscogsApi discogsApi;
    final Spotify spotify;
    private final IPieableList<ReturnNowPlaying, T> pie;

    public WhoKnowsBaseCommand(ChuuService dao) {
        super(dao);
        assert this.parser instanceof DaoParser<?>;
        ((DaoParser<?>) parser).setExpensiveSearch(true);
        ((DaoParser<?>) parser).setAllowUnaothorizedUsers(true);
        pie = new PieableListKnows<>(parser);
        parser.addOptional(new OptionalEntity("list", "display in list format"));
        this.respondInPrivate = false;
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        T parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        WhoKnowsMode whoknowsMode = getWhoknowsMode(parse);
        WrapperReturnNowPlaying wrapperReturnNowPlaying = generateWrapper(parse, whoknowsMode);
        if (wrapperReturnNowPlaying == null) {
            return;
        }
        generateWhoKnows(wrapperReturnNowPlaying, parse, e.getAuthor().getIdLong(), whoknowsMode);

    }

    abstract WhoKnowsMode getWhoknowsMode(T params);

    abstract WrapperReturnNowPlaying generateWrapper(T params, WhoKnowsMode whoKnowsMode) throws LastFmException;

    public void generateWhoKnows(WrapperReturnNowPlaying wrapperReturnNowPlaying, T ap, long author, WhoKnowsMode effectiveMode) {
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x -> x.setDiscordName(CommandUtil.getUserInfoNotStripped(ap.getE(), x.getDiscordId()).getUsername()));
        switch (effectiveMode) {

            case IMAGE:
                doImage(ap, wrapperReturnNowPlaying);

                break;
            case LIST:
                doList(ap, wrapperReturnNowPlaying);
                break;
            case PIE:
                doPie(ap, wrapperReturnNowPlaying);
                break;
        }
    }

    BufferedImage doImage(T ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        MessageReceivedEvent e = ap.getE();

        BufferedImage logo = null;
        String title;
        if (e.isFromGuild()) {
            logo = CommandUtil.getLogo(getService(), e);
            title = e.getGuild().getName();
        } else {
            title = e.getJDA().getSelfUser().getName();
        }
        BufferedImage image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, title, logo);
        sendImage(image, e);

        return logo;
    }

    void doList(T ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();
        StringBuilder builder = new StringBuilder();

        MessageReceivedEvent e = ap.getE();


        int counter = 1;
        for (ReturnNowPlaying returnNowPlaying : wrapperReturnNowPlaying.getReturnNowPlayings()) {
            builder.append(counter++)
                    .append(PrivacyUtils.toString(returnNowPlaying));
            if (counter == 11)
                break;
        }
        String usable;
        if (e.isFromGuild()) {
            usable = CommandUtil.cleanMarkdownCharacter(e.getGuild().getName());
        } else {
            usable = e.getJDA().getSelfUser().getName();
        }
        embedBuilder.setTitle(getTitle(ap, usable)).
                setThumbnail(CommandUtil.noImageUrl(wrapperReturnNowPlaying.getUrl())).setDescription(builder)
                .setColor(CommandUtil.randomColor());
        e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build())
                .queue(message1 ->
                        new Reactionary<>(wrapperReturnNowPlaying
                                .getReturnNowPlayings(), message1, embedBuilder));
    }

    void doPie(T ap, WrapperReturnNowPlaying returnNowPlaying) {
        PieChart pieChart = this.pie.doPie(ap, returnNowPlaying.getReturnNowPlayings());
        String usable;
        MessageReceivedEvent e = ap.getE();
        if (e.isFromGuild()) {
            usable = CommandUtil.cleanMarkdownCharacter(e.getGuild().getName());
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
