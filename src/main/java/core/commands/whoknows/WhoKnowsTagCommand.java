package core.commands.whoknows;

import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.GenreParser;
import core.parsers.Parser;
import core.parsers.params.GenreParameters;
import dao.ServiceView;
import dao.entities.ScrobbledArtist;
import dao.entities.WhoKnowsMode;
import dao.entities.WrapperReturnNowPlaying;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static core.commands.whoknows.MultipleWhoKnowsTagCommand.formatTag;

public class WhoKnowsTagCommand extends WhoKnowsBaseCommand<GenreParameters> {
    public WhoKnowsTagCommand(ServiceView dao) {
        super(dao);
    }

    public WhoKnowsTagCommand(ServiceView dao, boolean isLongRunningCommand) {
        super(dao, true);
    }

    @Override
    WhoKnowsMode getWhoknowsMode(GenreParameters params) {
        return getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);
    }

    @Override
    public String slashName() {
        return "who-knows";
    }

    @Override
    public CommandCategory initCategory() {
        return CommandCategory.GENRES;
    }


    @Override
    WrapperReturnNowPlaying generateWrapper(GenreParameters params, WhoKnowsMode whoKnowsMode) {
        Context e = params.getE();
        CompletableFuture<Optional<ScrobbledArtist>> completableFuture = CompletableFuture.supplyAsync(() -> db.getTopInTag(params.getGenre(), e.getGuild().getIdLong()));
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                whoKnowsMode.equals(WhoKnowsMode.IMAGE) ?
                this.db.whoKnowsGenre(params.getGenre(), e.getGuild().getIdLong()) :
                this.db.whoKnowsGenre(params.getGenre(), e.getGuild().getIdLong(), Integer.MAX_VALUE);
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(e, "No one knows " + CommandUtil.escapeMarkdown(params.getGenre()));
            return null;
        }
        return formatTag(e, completableFuture, wrapperReturnNowPlaying);
    }

    @Override
    public String getTitle(GenreParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.escapeMarkdown(params.getGenre()) + " in " + baseTitle + "?";

    }


    @Override
    public Parser<GenreParameters> initParser() {
        return new GenreParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Who knows a given tag on the bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("whoknowsgenre", "whoknowstag", "wktag", "wkg", "wg");
    }

    @Override
    public String getName() {
        return "Who knows Genre";
    }
}
