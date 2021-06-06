package core.commands.whoknows;

import core.commands.utils.CommandUtil;
import core.parsers.MultipleArtistsParser;
import core.parsers.Parser;
import core.parsers.params.MultiArtistParameters;
import dao.ServiceView;
import dao.entities.ReturnNowPlaying;
import dao.entities.WhoKnowsMode;
import dao.entities.WrapperReturnNowPlaying;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MultipleWhoKnowsCommand extends WhoKnowsBaseCommand<MultiArtistParameters> {
    public MultipleWhoKnowsCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    WhoKnowsMode getWhoknowsMode(MultiArtistParameters params) {
        return getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);
    }

    private String join(Set<String> a) {
        return String.join(",", a);
    }


    @Override
    WrapperReturnNowPlaying generateWrapper(MultiArtistParameters params, WhoKnowsMode whoKnowsMode) {
        int i = whoKnowsMode.equals(WhoKnowsMode.IMAGE) ? 10 : Integer.MAX_VALUE;
        List<WrapperReturnNowPlaying> whoKnowsArtistSet = db.getWhoKnowsArtistSet(params.getArtists(), params.getE().getGuild().getIdLong(), i, null);
        if (whoKnowsArtistSet.isEmpty()) {
            sendMessageQueue(params.getE(), "No one knows " + CommandUtil.escapeMarkdown(join(params.getArtists())));
            return null;
        }
        WrapperReturnNowPlaying first = whoKnowsArtistSet.get(0);

        Map<String, ReturnNowPlaying> stringReturnNowPlayingMap = WhoKnowsLoonasCommand.groupByUser(whoKnowsArtistSet);
        WrapperReturnNowPlaying wrapperReturnNowPlaying = new WrapperReturnNowPlaying(
                stringReturnNowPlayingMap.
                        values().stream().sorted(Comparator.comparingInt(ReturnNowPlaying::getPlayNumber).reversed())
                        .toList(),
                0, first.getUrl(), ""
        );
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x -> x.setDiscordName(CommandUtil.getUserInfoUnescaped(params.getE(), x.getDiscordId()).getUsername()));
        wrapperReturnNowPlaying.setUrl((first.getUrl()));
        wrapperReturnNowPlaying.setArtist(whoKnowsArtistSet.stream().map(WrapperReturnNowPlaying::getArtist).collect(Collectors.joining(",")));
        return wrapperReturnNowPlaying;
    }

    @Override
    public String getTitle(MultiArtistParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.escapeMarkdown(join(params.getArtists())) + " in " + baseTitle + "?";

    }


    @Override
    public Parser<MultiArtistParameters> initParser() {
        return new MultipleArtistsParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Like whoknows but for a lot of artists at the same time";
    }

    @Override
    public List<String> getAliases() {
        return List.of("multiwhoknows", "mwk", "multiwk", "mw", "multiw");
    }

    @Override
    public String getName() {
        return "Multi Who Knows";
    }
}
