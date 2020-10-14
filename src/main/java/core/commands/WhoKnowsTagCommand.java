package core.commands;

import core.Chuu;
import core.exceptions.InstanceNotFoundException;
import core.parsers.GenreParser;
import core.parsers.Parser;
import core.parsers.params.GenreParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.WhoKnowsMode;
import dao.entities.WrapperReturnNowPlaying;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class WhoKnowsTagCommand extends WhoKnowsBaseCommand<GenreParameters> {
    public WhoKnowsTagCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    WhoKnowsMode getWhoknowsMode(GenreParameters params) {
        LastFMData lastFMData = params.getLastFMData();
        if (lastFMData == null) {
            try {
                return getService().computeLastFmData(params.getUser().getIdLong(), params.getE().getGuild().getIdLong()).getWhoKnowsMode();
            } catch (InstanceNotFoundException exception) {
                return WhoKnowsMode.IMAGE;
            }
        } else {
            return lastFMData.getWhoKnowsMode();
        }
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(GenreParameters params, WhoKnowsMode whoKnowsMode) {
        MessageReceivedEvent e = params.getE();
        CompletableFuture<Optional<ScrobbledArtist>> completableFuture = CompletableFuture.supplyAsync(() -> getService().getTopInTag(params.getGenre(), e.getGuild().getIdLong()));
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                whoKnowsMode.equals(WhoKnowsMode.IMAGE) ? this.getService().whoKnowsGenre(params.getGenre(), e.getGuild().getIdLong()) : this.getService().whoKnowsGenre(params.getGenre(), e.getGuild().getIdLong(), Integer.MAX_VALUE);
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(e, "No one knows " + CommandUtil.cleanMarkdownCharacter(params.getGenre()));
            return null;
        }
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x -> x.setDiscordName(CommandUtil.getUserInfoNotStripped(e, x.getDiscordId()).getUsername()));
        try {
            Optional<ScrobbledArtist> scrobbledArtist = completableFuture.get();
            scrobbledArtist.ifPresent((sc) -> wrapperReturnNowPlaying.setUrl(sc.getUrl()));
        } catch (InterruptedException | ExecutionException interruptedException) {
            Chuu.getLogger().warn(interruptedException.getMessage(), interruptedException);
        }
        return wrapperReturnNowPlaying;
    }

    @Override
    public String getTitle(GenreParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.cleanMarkdownCharacter(params.getGenre()) + " in " + baseTitle + "?";

    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<GenreParameters> getParser() {
        return new GenreParser(getService(), lastFM);
    }

    @Override
    public String getDescription() {
        return "Who knows a given tag on the bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("whoknowsgenre", "whoknowstag", "wktag", "wkg");
    }

    @Override
    public String getName() {
        return "Who knows Genre";
    }
}
