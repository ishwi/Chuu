package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class RecentListCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {

    public RecentListCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.NOW_PLAYING;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> initParser() {
        Map<Integer, String> map = new HashMap<>(1);
        map.put(LIMIT_ERROR, "The number introduced must be lower than 15");
        String s = "You can also introduce a number to vary the number of songs shown, defaults to " + 5 + ", max " + 15;
        return new NumberParser<>(new OnlyUsernameParser(db),
                5L,
                15L,
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
        String usable = getUserString(e, discordID, lastFmName);

        List<NowPlayingArtist> list = lastFM.getRecent(user, (int) limit);
        //Can't be empty because NoPLaysException
        NowPlayingArtist header = list.get(0);

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(ColorService.computeColor(e))
                .setThumbnail(CommandUtil.noImageUrl(header.url()))
                .setTitle(String.format("%s's last %d tracks", usable, limit),
                        CommandUtil.getLastFmUser(lastFmName));

        int counter = 1;
        for (NowPlayingArtist nowPlayingArtist : list) {
            embedBuilder.addField("Track #" + counter++ + ":", String.format("**%s** - %s | %s%n", CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.songName()), CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.artistName()), CommandUtil.cleanMarkdownCharacter(nowPlayingArtist
                    .albumName())), false);
        }

        e.sendMessage(embedBuilder.build()).queue();


    }

    @Override
    public String getName() {
        return "Recent Songs";
    }


}
