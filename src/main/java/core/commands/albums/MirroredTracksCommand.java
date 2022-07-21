package core.commands.albums;

import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.TrackDistributor;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.services.UserInfoService;
import core.services.tracklist.UserTrackListService;
import core.services.validators.AlbumValidator;
import core.util.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MirroredTracksCommand extends AlbumPlaysCommand {

    public MirroredTracksCommand(ServiceView dao) {

        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public String getDescription() {
        return "Compare yourself with another user on one specific album";
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        ArtistAlbumParser parser = new ArtistAlbumParser(db, lastFM);
        parser.setExpensiveSearch(true);
        return parser;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("comparetracks", "tracklistcompare", "tlc", "ctl");
    }


    @Override
    public String getName() {
        return "Track list comparison";
    }

    @Override
    protected void doSomethingWithAlbumArtist(ScrobbledArtist scrobbledArtist, String album, Context e, long who, ArtistAlbumParameters params) throws LastFmException, InstanceNotFoundException {
        User author = params.getE().getAuthor();
        LastFMData secondUser = params.getLastFMData();
        if (author.getIdLong() == secondUser.getDiscordId()) {
            sendMessageQueue(e, "You need to provide at least one other user (ping,discord id,tag format, u:username or lfm:lastfm_name )");
            return;
        }
        String artist = scrobbledArtist.getArtist();
        LastFMData ogData = db.findLastFMData(author.getIdLong());
        ScrobbledAlbum scrobbledAlbum = new AlbumValidator(db, lastFM).validate(scrobbledArtist.getArtistId(), artist, album);

        Optional<FullAlbumEntity> trackList1 = new UserTrackListService(db, ogData.getName()).getTrackList(scrobbledAlbum, ogData, scrobbledArtist.getUrl(), e);
        if (trackList1.isEmpty()) {
            sendMessageQueue(e, "Couldn't find a tracklist for " + CommandUtil.escapeMarkdown(scrobbledArtist.getArtist()
            ) + " - " + CommandUtil.escapeMarkdown(scrobbledAlbum.getAlbum()));
            return;

        }
        Optional<FullAlbumEntity> trackList2 = new UserTrackListService(db, secondUser.getName()).getTrackList(scrobbledAlbum, secondUser, scrobbledArtist.getUrl(), e);
        if (trackList2.isEmpty()) {
            sendMessageQueue(e, "Couldn't find a tracklist for " + CommandUtil.escapeMarkdown(scrobbledArtist.getArtist()
            ) + " - " + CommandUtil.escapeMarkdown(scrobbledAlbum.getAlbum()));
            return;
        }
        UserInfoService userInfoService = new UserInfoService(db);
        UserInfo userInfo = userInfoService.getUserInfo(ogData);
        userInfo.setUsername(CommandUtil.getUserInfoUnescaped(e, ogData.getDiscordId()).username());
        UserInfo userInfo2 = userInfoService.getUserInfo(secondUser);
        userInfo2.setUsername(CommandUtil.getUserInfoUnescaped(e, secondUser.getDiscordId()).username());
        BufferedImage bufferedImage = TrackDistributor.drawImageMirrored(trackList1.get(), trackList2.get(), userInfo, userInfo2);
        sendImage(bufferedImage, e);
    }
}
