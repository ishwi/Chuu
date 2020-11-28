package core.commands;

import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.PrivacyMode;
import dao.entities.ScrobbledArtist;
import dao.entities.WhoKnowsMode;
import dao.entities.WrapperReturnNowPlaying;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class GlobalWhoKnowSongCommand extends GlobalBaseWhoKnowCommand<ArtistAlbumParameters> {
    public GlobalWhoKnowSongCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    PrivacyMode obtainPrivacyMode(ArtistAlbumParameters params) {
        return params.getLastFMData().getPrivacyMode();
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistSongParser(getService(), lastFM);
    }

    @Override
    public String getDescription() {
        return "Like who knows song but for all bot users and keeping some privacy";
    }


    @Override
    public List<String> getAliases() {
        return Arrays.asList("gwktrack", "gwkt", "gwhoknowstrack");
    }


    @Override
    public String getName() {
        return "Global Who Knows Track";
    }

    @Override
    WhoKnowsMode getWhoknowsMode(ArtistAlbumParameters params) {
        return getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistAlbumParameters params, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(params.getArtist(), 0, null);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotify, true, !params.isNoredirect());
        MessageReceivedEvent e = params.getE();
        long artistId = scrobbledArtist.getArtistId();
        params.setScrobbledArtist(scrobbledArtist);
        long trackId = CommandUtil.trackValidate(getService(), scrobbledArtist, lastFM, params.getAlbum());
        WhoKnowsMode effectiveMode = getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);

        boolean b = params.hasOptional("nobotted");
        long author = params.getE().getAuthor().getIdLong();
        int limit = effectiveMode.equals(WhoKnowsMode.IMAGE) ? 10 : Integer.MAX_VALUE;
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                this.getService().getGlobalWhoKnowsTrack(limit, trackId, author, !b);
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(params.getE(), "No one knows " + CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist() + " - " + params.getAlbum()));
            return null;
        }
        return wrapperReturnNowPlaying;
    }

    @Override
    public String getTitle(ArtistAlbumParameters params, String ignored) {
        return "Who knows " + CommandUtil.cleanMarkdownCharacter(params.getArtist() + " - " + params.getAlbum()) + " in " + params.getE().getJDA().getSelfUser().getName() + "?";
    }
}
