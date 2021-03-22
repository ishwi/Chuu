package core.commands.stats;

import com.neovisionaries.i18n.CountryCode;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.imagerenderer.WorldMapRenderer;
import core.otherlisteners.Reactionary;
import core.parsers.NumberParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.NumberParameters;
import core.parsers.params.TimeFrameParameters;
import core.parsers.utils.CustomTimeFrame;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class CountryCommand extends ConcurrentCommand<NumberParameters<TimeFrameParameters>> {
    private final MusicBrainzService musicBrainz;

    public CountryCommand(ChuuService dao) {
        super(dao);
        this.musicBrainz = MusicBrainzServiceSingleton.getInstance();

    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<NumberParameters<TimeFrameParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be between 1 and 5");
        String s = "A number which represent the palette to use.\n" +
                "If it is not indicated it defaults to a random palette";
        NumberParser<TimeFrameParameters, TimerFrameParser> parser = new NumberParser<>(new TimerFrameParser(db, TimeFrameEnum.ALL),
                null,
                5,
                map, s, false, true, false);
        parser.addOptional(new OptionalEntity("list", "display in list format"));
        return parser;

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
    protected void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<TimeFrameParameters> params) throws LastFmException {


        Long pallete = (params.getExtraParam());
        TimeFrameParameters innerParams = params.getInnerParams();
        LastFMData user = innerParams.getLastFMData();
        String username = user.getName();
        long discordId = user.getDiscordId();
        CompletableFuture<Message> future = null;
        TimeFrameEnum time = innerParams.getTime();
        if (time.equals(TimeFrameEnum.SEMESTER) || time.equals(TimeFrameEnum.ALL)) {
            future = sendMessage(e, "Going to take a while ").submit();
        }

        List<ArtistInfo> topAlbums = lastFM.getTopArtists(user, new CustomTimeFrame(time), 10000).stream()
                .filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
                .toList();
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


        if (params.hasOptional("list")) {
            List<String> collect = map.entrySet().stream().sorted(Map.Entry.comparingByValue((Comparator.reverseOrder()))).map(t ->
                    ". **%s**: %d %s%n".formatted(CountryCode.getByCode(t.getKey().getCountryCode(), false).getName(), t.getValue(), CommandUtil.singlePlural(t.getValue(), "artist", "artist"))
            ).toList();

            StringBuilder a = new StringBuilder();
            for (int i = 0; i < collect.size() && i < 10; i++) {
                String s = collect.get(i);
                a.append(i + 1).append(s);
            }
            DiscordUserDisplay uInfo = CommandUtil.getUserInfoNotStripped(params.getE(), discordId);

            var embedBuilder = new EmbedBuilder()
                    .setDescription(a)
                    .setAuthor(String.format("%s's countries", uInfo.getUsername()), PrivacyUtils.getLastFmUser(user.getName()), uInfo.getUrlImage())
                    .setColor(ColorService.computeColor(e))
                    .setFooter("%s has artist from %d different %s".formatted(uInfo.getUsername(), collect.size(), CommandUtil.singlePlural(collect.size(), "country", "countries")), null);

            e.getChannel().sendMessage(embedBuilder.build()).queue(m ->
                    new Reactionary<>(collect, m, 10, embedBuilder));
        } else {
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

}
