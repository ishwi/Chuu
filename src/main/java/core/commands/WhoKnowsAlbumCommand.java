package core.commands;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.util.IPieable;
import core.commands.util.PieableKnows;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.WhoKnowsMaker;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistAlbumParser;
import core.parsers.OptionalEntity;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.params.ArtistParameters;
import core.parsers.params.OptionalParameter;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class WhoKnowsAlbumCommand extends ConcurrentCommand {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;
    private IPieable<ReturnNowPlaying, ArtistParameters> pie;

    public WhoKnowsAlbumCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = false;
        this.parser = new ArtistAlbumParser(dao, lastFM, new OptionalEntity("--list", "display in list format"));
        this.pie = new PieableKnows(this.parser);

        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
    }

    @Override
    public String getDescription() {
        return ("How many times the guild has heard an album!");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("wkalbum", "wka", "whoknowsalbum");
    }

    @Override
    public String getName() {
        return "Get guild Album plays";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] parsed;
        parsed = parser.parse(e);
        if (parsed == null) {
            return;
        }
        ArtistAlbumParameters ap = new ArtistAlbumParameters(parsed, e, new OptionalParameter("--list", 3), new OptionalParameter("--pie", 4));
        ScrobbledArtist validable = new ScrobbledArtist(ap.getArtist(), 0, "");
        CommandUtil.validate(getService(), validable, lastFM, discogsApi, spotify);
        ap.setScrobbledArtist(validable);
        doSomethingWithAlbumArtist(ap);
    }

    public void doSomethingWithAlbumArtist(ArtistAlbumParameters ap) throws LastFmException {

        MessageReceivedEvent e = ap.getE();
        ScrobbledArtist artist = ap.getScrobbledArtist();
        long id = e.getGuild().getIdLong();
        // Gets list of users registered in guild
        List<UsersWrapper> userList = getService().getAll(id);
        if (userList.isEmpty()) {
            sendMessageQueue(e, "There are no users registered on this server");
            return;
        }

        // Gets play number for each registered artist
        AlbumUserPlays urlContainter = new AlbumUserPlays("", "");
        List<Long> usersThatKnow = getService().whoKnows(artist.getArtistId(), id, 25).getReturnNowPlayings().stream()
                .map(ReturnNowPlaying::getDiscordId)
                .collect(Collectors.toList());

        if (!usersThatKnow.contains(ap.getDiscordId()))
            usersThatKnow.add(ap.getDiscordId());
        if (!usersThatKnow.contains(e.getAuthor().getIdLong()))
            usersThatKnow.add(e.getAuthor().getIdLong());

        userList = userList.stream().filter(x -> usersThatKnow.contains(x.getDiscordID())).collect(Collectors.toList());

        Map<UsersWrapper, Integer> userMapPlays = fillPlayCounter(userList, artist.getArtist(), ap.getAlbum(), urlContainter);

        String corrected_album = urlContainter.getAlbum() == null || urlContainter.getAlbum().isEmpty() ? ap.getAlbum()
                : urlContainter.getAlbum();
        String corrected_artist = urlContainter.getArtist() == null || urlContainter.getArtist().isEmpty() ? artist.getArtist()
                : urlContainter.getArtist();

        // Manipulate data in order to pass it to the image Maker
        BufferedImage logo = CommandUtil.getLogo(getService(), e);
        List<Map.Entry<UsersWrapper, Integer>> list = new ArrayList<>(userMapPlays.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        List<ReturnNowPlaying> list2 = list.stream().sequential().limit(ap.hasOptional("--pie") || ap.hasOptional("--list") ? Integer.MAX_VALUE : 10).map(t -> {
            long id2 = t.getKey().getDiscordID();
            ReturnNowPlaying np = new ReturnNowPlaying(id2, t.getKey().getLastFMName(), corrected_artist, t.getValue());
            np.setDiscordName(CommandUtil.getUserInfoNotStripped(e, id2).getUsername());
            return np;
        }).filter(x -> x.getPlayNumber() > 0).collect(Collectors.toList());
        if (list2.isEmpty()) {
            sendMessageQueue(e, String.format(" No one knows %s - %s", CommandUtil.cleanMarkdownCharacter(corrected_artist), CommandUtil.cleanMarkdownCharacter(corrected_album)));
            return;
        }


        doExtraThings(list2, id, artist.getArtistId(), corrected_album);

        WrapperReturnNowPlaying a = new WrapperReturnNowPlaying(list2, list.size(), urlContainter.getAlbum_url(),
                corrected_artist + " - " + corrected_album);
        if (ap.hasOptional("--pie")) {
            doPie(ap, a);
            return;
        }
        if (ap.hasOptional("--list")) {
            doList(ap, a);
            return;
        }
        BufferedImage sender = WhoKnowsMaker.generateWhoKnows(a, e.getGuild().getName(), logo);

        sendImage(sender, e);

    }

    void doExtraThings(List<ReturnNowPlaying> list2, long id, long artistId, String album) {
        ReturnNowPlaying crownUser = list2.get(0);
        getService().insertAlbumCrown(artistId, album, crownUser.getDiscordId(), id, crownUser.getPlayNumber());
    }

    Map<UsersWrapper, Integer> fillPlayCounter(List<UsersWrapper> userList, String artist, String album,
                                               AlbumUserPlays fillWithUrl) throws LastFmException {
        Map<UsersWrapper, Integer> userMapPlays = new HashMap<>();

        UsersWrapper usersWrapper = userList.get(0);
        AlbumUserPlays temp = lastFM.getPlaysAlbum_Artist(usersWrapper.getLastFMName(), artist, album);
        fillWithUrl.setAlbum_url(temp.getAlbum_url());
        fillWithUrl.setAlbum(temp.getAlbum());
        fillWithUrl.setArtist(temp.getArtist());
        userMapPlays.put(usersWrapper, temp.getPlays());
        userList.stream().skip(1).forEach(u -> {
            try {
                AlbumUserPlays albumUserPlays = lastFM.getPlaysAlbum_Artist(u.getLastFMName(), artist, album);
                userMapPlays.put(u, albumUserPlays.getPlays());
            } catch (LastFmException ex) {
                Chuu.getLogger().warn(ex.getMessage(), ex);
            }
        });
        return userMapPlays;
    }

    private void doList(ArtistAlbumParameters ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
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
        embedBuilder.setTitle("Who knows " + CommandUtil.cleanMarkdownCharacter(ap.getScrobbledArtist().getArtist()) + " - " + ap.getAlbum() + " in " + CommandUtil.cleanMarkdownCharacter(e.getGuild().getName()) + "?").
                setThumbnail(CommandUtil.noImageUrl(wrapperReturnNowPlaying.getUrl())).setDescription(builder)
                .setColor(CommandUtil.randomColor());
        //.setFooter("Command invoked by " + event.getMember().getLastFmId().getDiscriminator() + "" + LocalDateTime.now().format(DateTimeFormatter.ISO_WEEK_DATE).toApiFormat(), );
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel())
                .queue(message1 ->
                        executor.execute(() -> new Reactionary<>(wrapperReturnNowPlaying
                                .getReturnNowPlayings(), message1, embedBuilder)));
    }

    private void doPie(ArtistAlbumParameters ap, WrapperReturnNowPlaying returnNowPlaying) {
        PieChart pieChart = this.pie.doPie(null, returnNowPlaying.getReturnNowPlayings());
        pieChart.setTitle("Who knows " + (ap.getScrobbledArtist().getArtist()) + " - " + ap.getAlbum() + " in " + (ap.getE().getGuild().getName()) + "?");
        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();

        GraphicUtils.setQuality(g);
        pieChart.paint(g, 1000, 750);
        try {
            java.net.URL url = new java.net.URL(returnNowPlaying.getUrl());
            BufferedImage backgroundImage = ImageIO.read(url);
            g.drawImage(backgroundImage, 10, 750 - 10 - backgroundImage.getHeight(), null);
        } catch (IOException ex) {
            Chuu.getLogger().warn(ex.getMessage(), ex);

        }
        sendImage(bufferedImage, ap.getE());
    }


}
