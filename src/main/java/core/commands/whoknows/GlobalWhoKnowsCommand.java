package core.commands.whoknows;

import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.PrivacyMode;
import dao.entities.ScrobbledArtist;
import dao.entities.WhoKnowsMode;
import dao.entities.WrapperReturnNowPlaying;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class GlobalWhoKnowsCommand extends GlobalBaseWhoKnowCommand<ArtistParameters> {

    public GlobalWhoKnowsCommand(ChuuService dao) {
        super(dao);

    }

    @Override
    PrivacyMode obtainPrivacyMode(ArtistParameters params) {
        return params.getLastFMData().getPrivacyMode();
    }

    @Override
    public String getName() {
        return "Global Who Knows";
    }

    @Override
    public String getDescription() {
        return "Like who knows but for all bot users and keeping some privacy :flushed:";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalwhoknows", "gk", "gwk", "gw");
    }

    @Override
    WhoKnowsMode getWhoknowsMode(ArtistParameters params) {
        return getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistParameters params, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(params.getArtist(), 0, null);
        CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotify, true, !params.isNoredirect());
        params.setScrobbledArtist(scrobbledArtist);
        MessageReceivedEvent e = params.getE();
        long artistId = scrobbledArtist.getArtistId();
        WhoKnowsMode effectiveMode = getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);

        boolean b = CommandUtil.showBottedAccounts(params.getLastFMData(), params, db);

        long author = params.getE().getAuthor().getIdLong();
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                effectiveMode.equals(WhoKnowsMode.IMAGE) ? this.db.globalWhoKnows(artistId, b, author, hidePrivate(params)) : this.db.globalWhoKnows(artistId, Integer.MAX_VALUE, b, author, hidePrivate(params));
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(params.getE(), "No one knows " + CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist()));
            return null;
        }
        wrapperReturnNowPlaying.setUrl(scrobbledArtist.getUrl());
        return wrapperReturnNowPlaying;
    }

    @Override
    public String getTitle(ArtistParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.cleanMarkdownCharacter(params.getScrobbledArtist().getArtist()) + " in " + params.getE().getJDA().getSelfUser().getName() + "?";
    }


    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM, false);
    }
}
