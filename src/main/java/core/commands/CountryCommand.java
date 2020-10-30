package core.commands;

import core.exceptions.LastFmException;
import core.imagerenderer.WorldMapRenderer;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.NumberParameters;
import core.parsers.params.TimeFrameParameters;
import dao.ChuuService;
import dao.entities.ArtistInfo;
import dao.entities.Country;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class CountryCommand extends ConcurrentCommand<NumberParameters<TimeFrameParameters>> {
    private final MusicBrainzService musicBrainz;

    public CountryCommand(ChuuService dao) {
        super(dao);
        this.musicBrainz = MusicBrainzServiceSingleton.getInstance();

    }


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<NumberParameters<TimeFrameParameters>> getParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be between 1 and 5");
        String s = "A number which represent the palette to use.\n" +
                "If it is not indicated it defaults to a random palette";
        return new NumberParser<>(new TimerFrameParser(getService(), TimeFrameEnum.ALL),
                null,
                5,
                map, s, false, true, false);

    }

    @Override
    public String getUsageInstructions() {
        return super.getUsageInstructions() + "\nThe existing palettes are the following:\n" + "https://cdn.discordapp.com/attachments/698702434108964874/725486389801779282/unknown.png";
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
        NumberParameters<TimeFrameParameters> parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        Long pallete = (parse.getExtraParam());
        TimeFrameParameters params = parse.getInnerParams();
        String username = params.getLastFMData().getName();
        long discordId = params.getLastFMData().getDiscordId();
        CompletableFuture<Message> future = null;
        TimeFrameEnum time = params.getTime();
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
        Integer indexPalette;
        if (pallete != null)
            indexPalette = Math.toIntExact(pallete - 1);
        else
            indexPalette = null;
        byte[] b = WorldMapRenderer.generateImage(map, CommandUtil.getUserInfoNotStripped(e, discordId).getUsername(), indexPalette);
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
