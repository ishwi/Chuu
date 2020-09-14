package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
    protected CommandCategory getCategory() {
        return CommandCategory.NOW_PLAYING;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> getParser() {
        Map<Integer, String> map = new HashMap<>(1);
        map.put(LIMIT_ERROR, "The number introduced must be lower than 15");
        String s = "You can also introduce a number to vary the number of songs shown, defaults to " + 5 + ", max " + 15;
        return new NumberParser<>(new OnlyUsernameParser(getService()),
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
    protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        NumberParameters<ChuuDataParams> params = parser.parse(e);
        if (params == null) {
            return;
        }
        long limit = params.getExtraParam();
        ChuuDataParams innerParams = params.getInnerParams();
        String lastFmName = innerParams.getLastFMData().getName();
        long discordID = innerParams.getLastFMData().getDiscordId();
        String usable = getUserString(e, discordID, lastFmName);

        List<NowPlayingArtist> list = lastFM.getRecent(lastFmName, (int) limit);
        //Can't be empty because NoPLaysException
        NowPlayingArtist header = list.get(0);

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(CommandUtil.noImageUrl(header.getUrl()))
                .setTitle(String.format("%s's last %d tracks", usable, limit),
                        CommandUtil.getLastFmUser(lastFmName));

        int counter = 1;
        for (NowPlayingArtist nowPlayingArtist : list) {
            embedBuilder.addField("Track #" + counter++ + ":", String.format("**%s** - %s | %s%n", CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getSongName()), CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getArtistName()), CommandUtil.cleanMarkdownCharacter(nowPlayingArtist
                    .getAlbumName())), false);
        }

        MessageBuilder messageBuilder = new MessageBuilder();
        e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build()).queue();


    }

    @Override
    public String getName() {
        return "Recent Songs";
    }


}
