package core.commands.stats;

import com.neovisionaries.i18n.CountryCode;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.imagerenderer.WorldMapRenderer;
import core.otherlisteners.Reactionary;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.NumberParameters;
import core.parsers.params.TimeFrameParameters;
import core.parsers.utils.CustomTimeFrame;
import core.parsers.utils.Optionals;
import core.scheduledtasks.ArtistMbidUpdater;
import dao.ServiceView;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class CountryCommand extends ConcurrentCommand<NumberParameters<TimeFrameParameters>> {
    private final MusicBrainzService musicBrainz;

    public CountryCommand(ServiceView dao) {
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
                map, s, false, true, false, "palette");
        parser.addOptional(Optionals.LIST.opt);
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
    protected void onCommand(Context e, @Nonnull NumberParameters<TimeFrameParameters> params) throws LastFmException {


        Long palette = params.getExtraParam();
        TimeFrameParameters innerParams = params.getInnerParams();
        LastFMData user = innerParams.getLastFMData();
        String username = user.getName();
        long discordId = user.getDiscordId();
        CompletableFuture<Message> future = null;
        TimeFrameEnum time = innerParams.getTime();
        if (time.equals(TimeFrameEnum.SEMESTER) || time.equals(TimeFrameEnum.ALL)) {
            future = sendMessage(e, "Might take a while.").submit();
        }
        List<ArtistInfo> topArtists;
        if (time.equals(TimeFrameEnum.ALL)) {
            Supplier<List<ArtistInfo>> fromDb = () -> db.getUserArtistByMbid(username).stream().map(t -> new ArtistInfo(t.getUrl(), t.getArtist(), t.getArtistMbid())).toList();
            if (CommandUtil.rand.nextFloat() < 0.2f) {
                topArtists = new ArtistMbidUpdater(db, lastFM).updateAndGet(user).stream()
                        .filter(u -> u.getArtistMbid() != null && !u.getArtistMbid().isEmpty())
                        .map(t -> new ArtistInfo(t.getUrl(), t.getArtist(), t.getArtistMbid()))
                        .toList();
                if (topArtists == null) {
                    topArtists = fromDb.get();
                }
            } else {
                topArtists = fromDb.get();
            }

        } else {
            topArtists = lastFM.getTopArtists(user, new CustomTimeFrame(time), 10000).stream()
                    .filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
                    .toList();
        }
        if (topArtists.isEmpty()) {
            sendMessageQueue(e, "Was not able to find any country on %s 's artists".formatted(getUserString(e, discordId, username)));
            return;

        }
        Map<Country, Integer> map = musicBrainz.countryCount(topArtists);

        if (map.isEmpty()) {
            CommandUtil.handleConditionalMessage(future);
            sendMessageQueue(e, "Was not able to find any country on %s 's artists".formatted(getUserString(e, discordId, username)));
            return;
        }

        RemainingImagesMode mode = CommandUtil.getEffectiveMode(user.getRemainingImagesMode(), params);
        if (mode == RemainingImagesMode.LIST) {
            List<String> lines = map.entrySet().stream().sorted(Map.Entry.comparingByValue((Comparator.reverseOrder()))).map(t ->
                    {
                        CountryCode byCode = CountryCode.getByCode(t.getKey().countryCode(), false);
                        return ". **%s**: %d %s%n".formatted(byCode == null ? t.getKey().countryCode() : byCode.getName(), t.getValue(), CommandUtil.singlePlural(t.getValue(), "artist", "artists"));
                    }
            ).toList();

            StringBuilder a = new StringBuilder();
            for (int i = 0; i < lines.size() && i < 10; i++) {
                String s = lines.get(i);
                a.append(i + 1).append(s);
            }
            DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(params.getE(), discordId);

            var embedBuilder = new ChuuEmbedBuilder(e)
                    .setDescription(a)
                    .setAuthor(String.format("%s's countries", uInfo.username()), PrivacyUtils.getLastFmUser(user.getName()), uInfo.urlImage())
                    .setFooter("%s has artist from %d different %s".formatted(uInfo.username(), lines.size(), CommandUtil.singlePlural(lines.size(), "country", "countries")), null);

            e.sendMessage(embedBuilder.build()).queue(m ->
                    new Reactionary<>(lines, m, 10, embedBuilder));
        } else {
            Integer indexPalette;
            if (palette != null)
                indexPalette = Math.toIntExact(palette - 1);
            else
                indexPalette = null;
            byte[] b = WorldMapRenderer.generateImage(map, CommandUtil.getUserInfoUnescaped(e, discordId).username(), indexPalette);
            CommandUtil.handleConditionalMessage(future);
            if (b == null) {
                parser.sendError("Unknown error happened while creating the map", e);
                return;
            }

            if (b.length < 8388608)
                e.doSendImage(b, "cat.png", null);
            else
                e.sendMessage("Boot too big").queue();
        }
    }

}
