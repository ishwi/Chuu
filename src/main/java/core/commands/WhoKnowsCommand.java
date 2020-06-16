package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.WhoKnowsMaker;
import core.imagerenderer.util.IPieable;
import core.imagerenderer.util.PieableKnows;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.params.ChartParameters;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.imgscalr.Scalr;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;


public class WhoKnowsCommand extends ConcurrentCommand<ArtistParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;
    private final IPieable<ReturnNowPlaying, ArtistParameters> pie;

    public WhoKnowsCommand(ChuuService dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
        this.pie = new PieableKnows(this.parser);
        this.respondInPrivate = false;

    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<ArtistParameters> getParser() {
        ArtistParser parser = new ArtistParser(getService(), lastFM,
                new OptionalEntity("--list", "display in list format"));
        parser.setExpensiveSearch(true);
        parser.setAllowUnaothorizedUsers(true);
        return parser;
    }

    public static WhoKnowsMode getEffectiveMode(WhoKnowsMode whoKnowsMode, CommandParameters chartParameters) {
        boolean pie = chartParameters.hasOptional("--pie");
        boolean list = chartParameters.hasOptional("--list");
        if ((whoKnowsMode.equals(WhoKnowsMode.LIST) && !list && !pie) || (!whoKnowsMode.equals(WhoKnowsMode.LIST) && list)) {
            return WhoKnowsMode.LIST;
        } else if (whoKnowsMode.equals(WhoKnowsMode.PIE) && !pie || !whoKnowsMode.equals(WhoKnowsMode.PIE) && pie) {
            return WhoKnowsMode.PIE;
        } else {
            return WhoKnowsMode.IMAGE;
        }
    }


    @Override
    public String getDescription() {
        return "Returns List Of Users Who Know the inputted Artist";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("whoknows", "wk", "whoknowsnp", "wknp");
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ArtistParameters artistParameters = parser.parse(e);
        if (artistParameters == null)
            return;
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artistParameters.getArtist(), 0, null);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotify, true, !artistParameters.isNoredirect());
        artistParameters.setScrobbledArtist(scrobbledArtist);
        whoKnowsLogic(artistParameters);
    }

    void doImage(ArtistParameters ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        MessageReceivedEvent e = ap.getE();
        BufferedImage logo = CommandUtil.getLogo(getService(), e);
        BufferedImage image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, e.getGuild().getName(), logo);
        sendImage(image, e);

    }

    void doList(ArtistParameters ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();
        StringBuilder builder = new StringBuilder();

        MessageReceivedEvent e = ap.getE();


        int counter = 1;
        for (ReturnNowPlaying returnNowPlaying : wrapperReturnNowPlaying.getReturnNowPlayings()) {
            builder.append(counter++)
                    .append(returnNowPlaying.toString());
            if (counter == 11)
                break;
        }
        String usable;
        if (e.isFromGuild()) {
            usable = CommandUtil.cleanMarkdownCharacter(e.getGuild().getName());
        } else {
            usable = e.getJDA().getSelfUser().getName();
        }
        embedBuilder.setTitle("Who knows " + CommandUtil.cleanMarkdownCharacter(ap.getScrobbledArtist().getArtist()) + " in " + usable + "?").
                setThumbnail(CommandUtil.noImageUrl(wrapperReturnNowPlaying.getUrl())).setDescription(builder)
                .setColor(CommandUtil.randomColor());
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel())
                .queue(message1 ->
                        new Reactionary<>(wrapperReturnNowPlaying
                                .getReturnNowPlayings(), message1, embedBuilder));
    }

    void doPie(ArtistParameters ap, WrapperReturnNowPlaying returnNowPlaying) {
        PieChart pieChart = this.pie.doPie(ap, returnNowPlaying.getReturnNowPlayings());
        String usable;
        MessageReceivedEvent e = ap.getE();
        if (e.isFromGuild()) {
            usable = CommandUtil.cleanMarkdownCharacter(e.getGuild().getName());
        } else {
            usable = e.getJDA().getSelfUser().getName();
        }
        pieChart.setTitle("Who knows " + (ap.getScrobbledArtist().getArtist()) + " in " + usable + "?");
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

    void whoKnowsLogic(ArtistParameters ap) {

        ScrobbledArtist who = ap.getScrobbledArtist();
        long artistId = who.getArtistId();
        WhoKnowsMode effectiveMode = getEffectiveMode(ap.getLastFMData().getWhoKnowsMode(), ap);

        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                effectiveMode.equals(WhoKnowsMode.IMAGE) ? this.getService().whoKnows(artistId, ap.getE().getGuild().getIdLong()) : this.getService().whoKnows(artistId, ap.getE().getGuild().getIdLong(), Integer.MAX_VALUE);
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(ap.getE(), "No one knows " + CommandUtil.cleanMarkdownCharacter(who.getArtist()));
            return;
        }
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x -> x.setDiscordName(CommandUtil.getUserInfoNotStripped(ap.getE(), x.getDiscordId()).getUsername()));
        wrapperReturnNowPlaying.setUrl(who.getUrl());
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

    @Override
    public String getName() {
        return "Who Knows";
    }


}
