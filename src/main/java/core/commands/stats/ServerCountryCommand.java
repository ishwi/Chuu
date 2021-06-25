package core.commands.stats;

import com.neovisionaries.i18n.CountryCode;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.WorldMapRenderer;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import core.parsers.utils.Optionals;
import core.services.MbidFetcher;
import dao.ServiceView;
import dao.entities.ArtistInfo;
import dao.entities.Country;
import dao.entities.RemainingImagesMode;
import dao.exceptions.InstanceNotFoundException;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.io.FileUtils;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class ServerCountryCommand extends ConcurrentCommand<NumberParameters<CommandParameters>> {
    private final MusicBrainzService musicBrainz;

    public ServerCountryCommand(ServiceView dao) {
        super(dao, true);
        this.musicBrainz = MusicBrainzServiceSingleton.getInstance();
        this.respondInPrivate = false;


    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be between 1 and 5");
        String s = "A number which represent the palette to use.\n" +
                   "If it is not indicated it defaults to a random palette";
        NumberParser<CommandParameters, NoOpParser> parser = new NumberParser<>(new NoOpParser(),
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
        return "Map representation of this server scrobbled artists";
    }

    @Override
    public List<String> getAliases() {
        return List.of("servercountries", "scountries");
    }

    @Override
    public String getName() {
        return "Server Countries";
    }

    @Override
    protected void onCommand(Context e, @NotNull NumberParameters<CommandParameters> params) throws LastFmException {


        Long palette = params.getExtraParam();
        CompletableFuture<Message> future = null;
        List<ArtistInfo> topArtists;
        String name = e.getGuild().getName();
        RemainingImagesMode mode;

        try {
            mode = db.computeLastFmData(e.getAuthor().getIdLong(), e.getGuild().getIdLong()).getRemainingImagesMode();
        } catch (InstanceNotFoundException instanceNotFoundException) {
            mode = RemainingImagesMode.IMAGE;
        }
        mode = CommandUtil.getEffectiveMode(mode, params);

        Map<Country, Integer> map = new MbidFetcher(db, musicBrainz).doFetch(() ->
                        db.getServerArtistsByMbid(e.getGuild().getIdLong()).stream().map(t -> new ArtistInfo(t.getUrl(), t.getArtist(), t.getArtistMbid())).toList(),
                musicBrainz::countryCount,
                new HashMap<>(), (result, values) -> {
                    result.putAll(values);
                    return result;
                });

        if (map.isEmpty()) {
            sendMessageQueue(e, "Was not able to find any country on %s's artists".formatted(name));
            return;
        }

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

            var embedBuilder = new ChuuEmbedBuilder(e)
                    .setDescription(a)
                    .setAuthor(String.format("%s's countries", name), e.getGuild().getIconUrl())
                    .setFooter("%s has artist from %d different %s".formatted(name, lines.size(), CommandUtil.singlePlural(lines.size(), "country", "countries")), null);

            e.sendMessage(embedBuilder.build()).queue(m ->
                    new Reactionary<>(lines, m, 10, embedBuilder));
        } else {
            Integer indexPalette;
            if (palette != null)
                indexPalette = Math.toIntExact(palette - 1);
            else
                indexPalette = null;
            byte[] b = WorldMapRenderer.generateImage(map, name, indexPalette);

            if (b == null) {
                parser.sendError("Unknown error happened while creating the map", e);
                return;
            }
            if (b.length > e.getMaxFileSize()) {
                sendMessageQueue(e, "Cannot send image because the image size exceeds %s".formatted(FileUtils.byteCountToDisplaySize(b.length)));
            } else
                e.doSendImage(b, "cat.png", null);
        }
    }

}
