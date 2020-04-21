package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.WorldMapRenderer;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.TimeFrameParameters;
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

public class CountryCommand extends ConcurrentCommand<TimeFrameParameters> {
    private final MusicBrainzService musicBrainz;

    public CountryCommand(ChuuService dao) {
        super(dao);
        this.musicBrainz = MusicBrainzServiceSingleton.getInstance();

    }

    @Override
    public Parser<TimeFrameParameters> getParser() {
        return new TimerFrameParser(getService(), TimeFrameEnum.ALL);
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
        TimeFrameParameters returned = parser.parse(e);

        String username = returned.getLastFMData().getName();
        long discordId = returned.getLastFMData().getDiscordId();
        CompletableFuture<Message> future = null;
        TimeFrameEnum time = returned.getTime();
        if (time.equals(TimeFrameEnum.SEMESTER) || time.equals(TimeFrameEnum.ALL)) {
            future = sendMessage(e, "Going to take a while ").submit();
        }

        List<ArtistInfo> topAlbums = lastFM.getTopArtists(username, time.toApiFormat(), 10000).stream()
                .filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
                .collect(Collectors.toList());
        if (topAlbums.isEmpty()) {
            sendMessageQueue(e, "Was not able to find any country on " + getUserString(e, discordId, username) + " 's artists");
            return;

        }
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
