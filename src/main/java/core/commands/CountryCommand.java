package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.WorldMapRenderer;
import core.parsers.TimerFrameParser;
import dao.ChuuService;
import dao.entities.ArtistInfo;
import dao.entities.Country;
import dao.entities.TimeFrameEnum;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CountryCommand extends ConcurrentCommand {
    private final MusicBrainzService musicBrainz;

    public CountryCommand(ChuuService dao) {
        super(dao);
        this.parser = new TimerFrameParser(dao, TimeFrameEnum.ALL);
        this.musicBrainz = MusicBrainzServiceSingleton.getInstance();

    }

    @Override
    public String getDescription() {
        return "Map representation of your scrobbled artists";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("countries");
    }

    @Override
    public String getName() {
        return "My Countries";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned = parser.parse(e);

        String username = returned[0];
        long discordId = Long.parseLong(returned[1]);
        String timeframe = returned[2];
        CompletableFuture<Message> future = null;
        if (timeframe.equals(TimeFrameEnum.SEMESTER.toApiFormat()) || timeframe.equals(TimeFrameEnum.ALL.toApiFormat())) {
            future = sendMessage(e, "Going to take a while ").submit();
        }

        List<ArtistInfo> topAlbums = lastFM.getTopArtists(username, timeframe, 10000).stream()
                .filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
                .collect(Collectors.toList());

        Map<Country, Integer> map = musicBrainz.countryCount(topAlbums);

        if (map.isEmpty()) {
            CommandUtil.handleConditionalMessage(future);
            sendMessageQueue(e, "Was not able to find any country on " + getUserString(e, discordId, username) + " 's artists");
            return;
        }

        byte[] b = WorldMapRenderer.generateImage(map);
        CommandUtil.handleConditionalMessage(future);
        if (b == null) {
            parser.sendError("Unknown error happened while creating the map", e);
            return;
        }

        if (b.length < 8388608)
            e.getChannel().sendFile(b, "cat.png").queue();
        else
            e.getChannel().sendMessage("Boot too big").queue();

    }
}
