package core.commands.streaks;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistParser;
import core.parsers.NumberParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.params.NumberParameters;
import dao.ServiceView;
import dao.entities.*;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class TopArtistComboCommand extends ConcurrentCommand<NumberParameters<ArtistParameters>> {

    private final DiscogsApi discogsApi;
    private final Spotify spotify;


    public TopArtistComboCommand(ServiceView dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.STREAKS;
    }

    @Override
    public Parser<NumberParameters<ArtistParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce a number to only get streak with more than that number of plays. ";
        NumberParser<ArtistParameters, ArtistParser> parser = new NumberParser<>(new ArtistParser(db, lastFM),
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true, "filter");
        parser.addOptional(new OptionalEntity("server", "only include people in this server"));
        parser.addOptional(new OptionalEntity("me", "only include author of the command"));
        parser.addOptional(new OptionalEntity("myself", "same as `me`"));
        parser.addOptional(new OptionalEntity("start", "show the moment the streak started"));
        return parser;
    }

    @Override
    public String getDescription() {
        return "List of the top streaks for a specific artist in the bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("artistcombo", "artiststreaks", "acombo", "astreak", "streaka", "comboa");
    }

    @Override
    public String getName() {
        return "Top Artist Streaks";
    }

    @Override
    protected void onCommand(Context e, @NotNull NumberParameters<ArtistParameters> params) throws LastFmException {

        Long author = e.getAuthor().getIdLong();

        Long guildId = null;
        String title;
        Integer limit = params.getExtraParam() == null ? null : Math.toIntExact(params.getExtraParam());
        boolean myself = params.hasOptional("me") || params.hasOptional("myself");
        if (myself) {
            DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, params.getInnerParams().getLastFMData().getDiscordId());
            title = uInfo.getUsername();
        } else if (e.isFromGuild() && params.hasOptional("server")) {
            Guild guild = e.getGuild();
            guildId = guild.getIdLong();
            title = guild.getName();
        } else {
            SelfUser selfUser = e.getJDA().getSelfUser();
            title = selfUser.getName();
        }
        ArtistParameters innerParams = params.getInnerParams();
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(innerParams.getArtist(), 0, "");
        CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotify, true, !innerParams.isNoredirect());
        List<? extends StreakEntity> topStreaks;
        if (myself) {
            topStreaks = db.getUserArtistTopStreaks(params.getInnerParams().getLastFMData().getDiscordId(), scrobbledArtist.getArtistId(), limit);
        } else {
            topStreaks = db.getArtistTopStreaks(params.getExtraParam(), guildId, scrobbledArtist.getArtistId(), limit);
        }
        Set<Long> showableUsers;
        if (params.getE().isFromGuild()) {
            showableUsers = db.getAll(params.getE().getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
            showableUsers.add(author);
        } else {
            showableUsers = Set.of(author);
        }
        AtomicInteger atomicInteger = new AtomicInteger(1);
        AtomicInteger positionCounter = new AtomicInteger(1);


        Function<StreakEntity, String> mapper = (x) -> {
            String s;
            String lastfmId;
            if (x instanceof GlobalStreakEntities t) {
                PrivacyUtils.PrivateString publicString = PrivacyUtils.getPublicString(t.getPrivacyMode(), t.getDiscordId(), t.getLastfmId(), atomicInteger, e, showableUsers);
                s = publicString.discordName();
                lastfmId = t.getLastfmId();
            } else {
                s = title;
                lastfmId = params.getInnerParams().getLastFMData().getName();
            }
            int andIncrement = positionCounter.getAndIncrement();
            String dayNumberSuffix = CommandUtil.getDayNumberSuffix(andIncrement);
            String formatted = "%s **%s**%n".formatted(dayNumberSuffix, s);

            String aString = LinkUtils.cleanMarkdownCharacter(x.getCurrentArtist());
            StringBuilder description = new StringBuilder(formatted);

            GlobalStreakEntities.DateHolder holder = params.hasOptional("start") ? CommandUtil.toDateHolder(x.getStreakStart(), lastfmId) : null;

            return GlobalStreakEntities.getComboString(aString, description, x.artistCount(), x.getCurrentArtist(), x.albumCount(), x.getCurrentAlbum(), x.trackCount(), x.getCurrentSong(), holder);
        };

        if (topStreaks.isEmpty()) {
            sendMessageQueue(e, title + " doesn't have any stored streaks.");
            return;
        }

        List<Memoized<StreakEntity, String>> z = topStreaks.stream().map(t -> new Memoized<>(t, mapper)).toList();


        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 5 && i < z.size(); i++) {
            a.append(i + 1).append(z.get(i).toString());
        }

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .
                        setAuthor(String.format("%s's top streaks in %s ", scrobbledArtist.getArtist(), CommandUtil.escapeMarkdown(title)))
                .setThumbnail(scrobbledArtist.getUrl())
                .setDescription(a)
                .setFooter(String.format("%s has a total of %d %s %s!", CommandUtil.escapeMarkdown(title), topStreaks.size(), scrobbledArtist.getArtist(), CommandUtil.singlePlural(topStreaks.size(), "streak", "streaks")));
        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(z, message1, 5, embedBuilder));
    }
}
