package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.ListSender;
import core.exceptions.LastFmException;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.Rank;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class RecentListCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {

    public RecentListCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.NOW_PLAYING;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> initParser() {
        Map<Integer, String> map = new HashMap<>(1);
        map.put(LIMIT_ERROR, "The number introduced must be lower than 1000");
        String s = "You can also introduce a number to vary the number of songs shown, defaults to " + 5 + ", max " + 1000;
        return new NumberParser<>(new OnlyUsernameParser(db),
                5L,
                1000L,
                map, s, false, true);
    }

    @Override
    public String getDescription() {
        return "Returns your most recent songs played";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("recent");
    }

    @Override
    protected void onCommand(Context e, @NotNull NumberParameters<ChuuDataParams> params) throws LastFmException {


        long limit = params.getExtraParam();
        ChuuDataParams innerParams = params.getInnerParams();
        LastFMData user = innerParams.getLastFMData();
        String lastFmName = user.getName();
        long discordID = user.getDiscordId();
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, discordID);

        List<NowPlayingArtist> list = lastFM.getRecent(user, (int) limit);
        //Can't be empty because NoPLaysException
        NowPlayingArtist header = list.get(0);

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setThumbnail(CommandUtil.noImageUrl(header.url()))
                .setAuthor(String.format("%s's last %d tracks", uInfo.username(), limit),
                        CommandUtil.getLastFmUser(lastFmName), uInfo.urlImage());

        AtomicInteger ranker = new AtomicInteger(0);
        List<Rank<NowPlayingArtist>> elements = list.stream().map(w -> new Rank<>(w, ranker.incrementAndGet())).toList();
        new ListSender<>(e, elements, rank -> {
            NowPlayingArtist np = rank.entity();
            return "**Track #%d:**%n%s%n".formatted(rank.rank(),
                    String.format("**%s** - %s | %s%n", CommandUtil.escapeMarkdown(np.songName()), CommandUtil.escapeMarkdown(np.artistName()), CommandUtil.escapeMarkdown(np
                            .albumName())));
        }, embedBuilder)
                .doSend(false);


    }

    @Override
    public String getName() {
        return "Recent Songs";
    }


}
