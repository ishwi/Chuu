package core.commands.whoknows;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ServiceView;
import dao.entities.*;

import java.util.*;
import java.util.stream.Collectors;

public class WhoKnowsAlbumCommand extends WhoKnowsBaseCommand<ArtistAlbumParameters> {

    public WhoKnowsAlbumCommand(ServiceView dao) {
        super(dao);
    }


    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistAlbumParser(db, lastFM, false);
    }

    @Override
    public String getDescription() {
        return ("How many times the guild has heard an album!");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("uwkalbum", "uwka", "uwhoknowsalbum", "uwa");
    }

    @Override
    public String getName() {
        return "Updated Who Knows Album";
    }


    @Override
    WhoKnowsMode getWhoknowsMode(ArtistAlbumParameters params) {
        return getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistAlbumParameters ap, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist validable = new ScrobbledArtist(ap.getArtist(), 0, "");
        CommandUtil.validate(db, validable, lastFM, discogsApi, spotify, true, !ap.isNoredirect());
        ap.setScrobbledArtist(validable);
        Context e = ap.getE();
        ScrobbledArtist artist = ap.getScrobbledArtist();
        long id = e.getGuild().getIdLong();
        // Gets list of users registered in guild
        List<UsersWrapper> userList = db.getAll(id);
        if (userList.isEmpty()) {
            sendMessageQueue(e, "There are no users registered on this server");
            return null;
        }

        // Gets play number for each registered artist
        AlbumUserPlays urlContainter = new AlbumUserPlays("", "");
        Set<Long> usersThatKnow = db.whoKnows(artist.getArtistId(), id, 25).getReturnNowPlayings().stream()
                .map(ReturnNowPlaying::getDiscordId)
                .collect(Collectors.toSet());

        usersThatKnow.add(ap.getLastFMData().getDiscordId());
        usersThatKnow.add(e.getAuthor().getIdLong());

        userList = userList.stream()
                .filter(x ->
                        usersThatKnow.contains(x.getDiscordID()) || x.getDiscordID() == ap.getLastFMData().getDiscordId() || x.getDiscordID() == e.getAuthor().getIdLong())
                .toList();
        if (userList.isEmpty()) {
            Chuu.getLogger().error("Something went real wrong");
            sendMessageQueue(e, String.format(" No one knows %s - %s", CommandUtil.escapeMarkdown(ap.getArtist()), CommandUtil.escapeMarkdown(ap.getAlbum())));
            return null;
        }
        Map<UsersWrapper, Integer> userMapPlays = fillPlayCounter(userList, artist.getArtist(), ap.getAlbum(), urlContainter);

        String correctedAlbum = urlContainter.getAlbum() == null || urlContainter.getAlbum().isEmpty() ? ap.getAlbum()
                                                                                                       : urlContainter.getAlbum();
        String correctedArtist = urlContainter.getArtist() == null || urlContainter.getArtist().isEmpty() ? artist.getArtist()
                                                                                                          : urlContainter.getArtist();

        // Manipulate data in order to pass it to the image Maker
        List<Map.Entry<UsersWrapper, Integer>> userCounts = new ArrayList<>(userMapPlays.entrySet());
        userCounts.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        WhoKnowsMode effectiveMode = WhoKnowsCommand.getEffectiveMode(ap.getLastFMData().getWhoKnowsMode(), ap);
        List<ReturnNowPlaying> list2 = userCounts.stream().sequential().limit(effectiveMode.equals(WhoKnowsMode.IMAGE) ? 10 : Integer.MAX_VALUE).map(t -> {
            long id2 = t.getKey().getDiscordID();
            ReturnNowPlaying np = new ReturnNowPlayingAlbum(id2, t.getKey().getLastFMName(), correctedArtist, t.getValue(), correctedAlbum);
            np.setDiscordName(CommandUtil.getUserInfoUnescaped(e, id2).getUsername());
            return np;
        }).filter(x -> x.getPlayNumber() > 0).toList();
        if (list2.isEmpty()) {
            sendMessageQueue(e, String.format(" No one knows %s - %s", CommandUtil.escapeMarkdown(correctedArtist), CommandUtil.escapeMarkdown(correctedAlbum)));
            return null;
        }


        doExtraThings(list2, id, artist.getArtistId(), correctedAlbum);

        return new WrapperReturnNowPlaying(list2, userCounts.size(), Chuu.getCoverService().getCover(artist.getArtistId(), urlContainter.getAlbumUrl(), ap.getE()),
                correctedArtist + " - " + correctedAlbum);
    }


    void doExtraThings(List<ReturnNowPlaying> list2, long id, long artistId, String album) {
        ReturnNowPlaying crownUser = list2.get(0);
        db.insertAlbumCrown(artistId, album, crownUser.getDiscordId(), id, crownUser.getPlayNumber());
    }

    Map<UsersWrapper, Integer> fillPlayCounter(List<UsersWrapper> userList, String artist, String album,
                                               AlbumUserPlays fillWithUrl) throws LastFmException {
        Map<UsersWrapper, Integer> userMapPlays = new HashMap<>();

        UsersWrapper usersWrapper = userList.get(0);
        AlbumUserPlays temp = lastFM.getPlaysAlbumArtist(LastFMData.ofUserWrapper(usersWrapper), artist, album);
        fillWithUrl.setAlbumUrl(temp.getAlbumUrl());
        fillWithUrl.setAlbum(temp.getAlbum());
        fillWithUrl.setArtist(temp.getArtist());
        userMapPlays.put(usersWrapper, temp.getPlays());
        userList.stream().skip(1).forEach(u -> {
            try {
                AlbumUserPlays albumUserPlays = lastFM.getPlaysAlbumArtist(LastFMData.ofUserWrapper(u), artist, album);
                userMapPlays.put(u, albumUserPlays.getPlays());
            } catch (LastFmException ex) {
                Chuu.getLogger().warn(ex.getMessage(), ex);
            }
        });
        return userMapPlays;
    }


    @Override
    public String getTitle(ArtistAlbumParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.escapeMarkdown(params.getArtist() + " - " + params.getAlbum()) + " in " + baseTitle + "?";
    }


}
