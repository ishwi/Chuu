package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ExtraParser;
import core.parsers.OnlyUsernameParser;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RecentListCommand extends ConcurrentCommand {

    public RecentListCommand(ChuuService dao) {
        super(dao);
        Pattern compile = Pattern.compile("\\d+");
        Map<Integer, String> map = new HashMap<>(1);
        map.put(10, "The number introduced must be lower than 15");
        this.parser = new ExtraParser<>(new OnlyUsernameParser(getService()),
                5,
                (s) -> compile.matcher(s).matches(),
                (number) -> number > 15,
                Integer::parseInt,
                String::valueOf,
                map,
                "number",
                "You can also introduce a number to vary the number of songs shown, defaults to 5, max 15");
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
        String[] returned = parser.parse(e);
        if (returned == null) {
            return;
        }
        int limit = Integer.parseInt(returned[0]);
        String lastFmName = returned[1];
        long discordID = Long.parseLong(returned[2]);
        String usable = getUserStringConsideringGuildOrNot(e, discordID, lastFmName);

        List<NowPlayingArtist> list = lastFM.getRecent(lastFmName, limit);
        //Can't be empty because NoPLaysException
        NowPlayingArtist header = list.get(0);

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(CommandUtil.noImageUrl(header.getUrl()))
                .setTitle("" + usable + "'s last " + limit + " tracks",
                        CommandUtil.getLastFmUser(lastFmName));

        int counter = 1;
        for (NowPlayingArtist nowPlayingArtist : list) {
            embedBuilder.addField("Track #" + counter++ + ":", "**" + nowPlayingArtist.getSongName() +
                                                               "** - " + nowPlayingArtist.getArtistName() + " | " + nowPlayingArtist
                                                                       .getAlbumName() + "\n", false);
        }

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();


    }

    @Override
    public String getName() {
        return "Recent Songs";
    }


}
