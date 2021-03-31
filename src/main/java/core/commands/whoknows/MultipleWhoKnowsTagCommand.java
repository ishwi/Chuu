package core.commands.whoknows;

import core.Chuu;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.MultipleGenresParser;
import core.parsers.Parser;
import core.parsers.params.MultipleGenresParameters;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MultipleWhoKnowsTagCommand extends WhoKnowsBaseCommand<MultipleGenresParameters> {
    public MultipleWhoKnowsTagCommand(ChuuService dao) {
        super(dao);
    }

    @NotNull
    static WrapperReturnNowPlaying formatTag(MessageReceivedEvent e, CompletableFuture<Optional<ScrobbledArtist>> completableFuture, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
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
    WhoKnowsMode getWhoknowsMode(MultipleGenresParameters params) {
        LastFMData lastFMData = params.getLastFMData();
        if (lastFMData == null) {
            try {
                if (params.getE().isFromGuild())
                    return db.computeLastFmData(params.getE().getAuthor().getIdLong(), params.getE().getGuild().getIdLong()).getWhoKnowsMode();
                return WhoKnowsMode.IMAGE;
            } catch (InstanceNotFoundException exception) {
                return WhoKnowsMode.IMAGE;
            }
        } else {
            return lastFMData.getWhoKnowsMode();
        }
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(MultipleGenresParameters params, WhoKnowsMode whoKnowsMode) {
        MessageReceivedEvent e = params.getE();
        SearchMode mode = params.getMode();
        CompletableFuture<Optional<ScrobbledArtist>> completableFuture = CompletableFuture.supplyAsync(() -> db.getTopInTag(params.getGenres(), e.getGuild().getIdLong(), mode));

        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                whoKnowsMode.equals(WhoKnowsMode.IMAGE) ?
                        this.db.getWhoKnowsTagSet(params.getGenres(), e.getGuild().getIdLong(), Integer.MAX_VALUE, null, mode) :
                        this.db.getWhoKnowsTagSet(params.getGenres(), e.getGuild().getIdLong(), Integer.MAX_VALUE, null, mode);
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(e, "No one knows " + CommandUtil.cleanMarkdownCharacter(params.getGenres().stream().map(WordUtils::capitalizeFully).collect(Collectors.joining(","))));
            return null;
        }

        return formatTag(e, completableFuture, wrapperReturnNowPlaying);
    }

    @Override
    public String getTitle(MultipleGenresParameters params, String baseTitle) {
        return "mwkt";
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<MultipleGenresParameters> initParser() {
        return new MultipleGenresParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Returns a list of all the artist that match multiple tags";
    }

    public List<String> getAliases() {
        return List.of("multiplewhoknowsgenre", "multiwhoknowstag", "multiwktag", "multiwkg", "mwkg", "mwkt", "mwg");
    }


    @Override
    public String getName() {
        return "Multiple Who Knows Tag";
    }
}
