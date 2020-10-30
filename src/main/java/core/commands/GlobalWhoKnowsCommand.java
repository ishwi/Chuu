package core.commands;

import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
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
    public String getName() {
        return "Global Who Knows";
    }

    @Override
    public String getDescription() {
        return "Like who knows but for all bot users and keeping some privacy :flushed:";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalwhoknows", "gk", "gwk");
    }

    @Override
    WhoKnowsMode getWhoknowsMode(ArtistParameters params) {
        return getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistParameters params, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(params.getArtist(), 0, null);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotify, true, !params.isNoredirect());
        params.setScrobbledArtist(scrobbledArtist);
        MessageReceivedEvent e = params.getE();
        long artistId = scrobbledArtist.getArtistId();
        WhoKnowsMode effectiveMode = getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);

        boolean b = params.hasOptional("nobotted");
        long author = params.getE().getAuthor().getIdLong();
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                effectiveMode.equals(WhoKnowsMode.IMAGE) ? this.getService().globalWhoKnows(artistId, !b, author) : this.getService().globalWhoKnows(artistId, Integer.MAX_VALUE, !b, author);
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
        return new ArtistParser(getService(), lastFM);
    }
}
