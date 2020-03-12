package core.commands;

import core.Chuu;
import core.exceptions.LastFmException;
import core.imagerenderer.WhoKnowsMaker;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

public class WhoKnowsAlbumCommand extends AlbumPlaysCommand {

    public WhoKnowsAlbumCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = false;
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
    public void doSomethingWithAlbumArtist(ScrobbledArtist artist, String album, MessageReceivedEvent e, long who) {

        long id = e.getGuild().getIdLong();
        // Gets list of users registered in guild
        List<UsersWrapper> userList = getService().getAll(id);
        if (userList.isEmpty()) {
            sendMessageQueue(e, "No users are registered on this server");
            return;
        }

        // Gets play number for each registered artist
        AlbumUserPlays urlContainter = new AlbumUserPlays("", "");
        List<Long> usersThatKnow = getService().whoKnows(artist.getArtistId(), id, Integer.MAX_VALUE).getReturnNowPlayings().stream()
                .map(ReturnNowPlaying::getDiscordId)
                .collect(Collectors.toList());

        if (!usersThatKnow.contains(who))
            usersThatKnow.add(who);

        userList = userList.stream().filter(x -> usersThatKnow.contains(x.getDiscordID())).collect(Collectors.toList());

        Map<UsersWrapper, Integer> userMapPlays = fillPlayCounter(userList, artist.getArtist(), album, urlContainter);

        String corrected_album = urlContainter.getAlbum() == null || urlContainter.getAlbum().isEmpty() ? album
                : urlContainter.getAlbum();
        String corrected_artist = urlContainter.getArtist() == null || urlContainter.getArtist().isEmpty() ? artist.getArtist()
                : urlContainter.getArtist();

        // Manipulate data in order to pass it to the image Maker
        BufferedImage logo = CommandUtil.getLogo(getService(), e);
        List<Map.Entry<UsersWrapper, Integer>> list = new ArrayList<>(userMapPlays.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        List<ReturnNowPlaying> list2 = list.stream().sequential().limit(10).map(t -> {
            long id2 = t.getKey().getDiscordID();
            ReturnNowPlaying np = new ReturnNowPlaying(id2, t.getKey().getLastFMName(), corrected_artist, t.getValue());
            np.setDiscordName(getUserString(e, id2, t.getKey().getLastFMName()));
            return np;
        }).filter(x -> x.getPlayNumber() > 0).collect(Collectors.toList());
        if (list2.isEmpty()) {
            sendMessageQueue(e, " No one knows " + corrected_artist + " - " + corrected_album);
            return;
        }

        doExtraThings(list2, id, artist.getArtistId(), corrected_album);

        WrapperReturnNowPlaying a = new WrapperReturnNowPlaying(list2, 0, urlContainter.getAlbum_url(),
                corrected_artist + " - " + corrected_album);

        BufferedImage sender = WhoKnowsMaker.generateWhoKnows(a, e.getGuild().getName(), logo);

        sendImage(sender, e);

    }

    void doExtraThings(List<ReturnNowPlaying> list2, long id, long artistId, String album) {
        ReturnNowPlaying crownUser = list2.get(0);
        getService().insertAlbumCrown(artistId, album, crownUser.getDiscordId(), id, crownUser.getPlayNumber());
    }

    Map<UsersWrapper, Integer> fillPlayCounter(List<UsersWrapper> userList, String artist, String album,
                                               AlbumUserPlays fillWithUrl) {
        Map<UsersWrapper, Integer> userMapPlays = new HashMap<>();
        userList.forEach(u -> {
            try {

                AlbumUserPlays albumUserPlays = lastFM.getPlaysAlbum_Artist(u.getLastFMName(), artist, album);
                if (fillWithUrl.getAlbum_url().isEmpty()) {
                    fillWithUrl.setAlbum_url(albumUserPlays.getAlbum_url());
                }
                if (fillWithUrl.getAlbum().isEmpty()) {
                    fillWithUrl.setAlbum(albumUserPlays.getAlbum());
                }
                if (fillWithUrl.getArtist() == null || fillWithUrl.getArtist().isEmpty()) {
                    fillWithUrl.setArtist(albumUserPlays.getArtist());
                }
                userMapPlays.put(u, albumUserPlays.getPlays());
            } catch (LastFmException ex) {
                Chuu.getLogger().warn(ex.getMessage(), ex);
            }
        });
        return userMapPlays;
    }

}
