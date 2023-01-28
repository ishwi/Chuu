package core.commands.streaks;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import core.parsers.utils.Optionals;
import core.util.ServiceView;
import dao.entities.GlobalStreakEntities;
import dao.entities.UsersWrapper;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class TopCombosCommand extends ConcurrentCommand<NumberParameters<CommandParameters>> {

    public TopCombosCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.STREAKS;
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce a number to only get streak with more than that number of plays. ";
        NumberParser<CommandParameters, NoOpParser> parser = new NumberParser<>(NoOpParser.INSTANCE,
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true, "filter");
        parser.addOptional(Optionals.SERVER.opt);
        parser.addOptional(Optionals.SERVER.opt);
        return parser;
    }

    @Override
    public String getDescription() {
        return "List of the top streaks in the bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("botstreaks", "topcombos", "topstreaks", "tc");
    }

    @Override
    public String getName() {
        return "Top Streaks";
    }

    @Override
    public void onCommand(Context e, @NotNull NumberParameters<CommandParameters> params) {

        Long author = e.getAuthor().getIdLong();

        Long guildId = null;
        String title;
        String validUrl;
        if (e.isFromGuild() && params.hasOptional("server")) {
            Guild guild = e.getGuild();
            guildId = guild.getIdLong();
            title = guild.getName();
            validUrl = guild.getIconUrl();
        } else {
            SelfUser selfUser = e.getJDA().getSelfUser();
            title = selfUser.getName();
            validUrl = selfUser.getAvatarUrl();
        }
        List<GlobalStreakEntities> topStreaks = db.getTopStreaks(params.getExtraParam(), guildId);
        if (topStreaks.isEmpty()) {
            sendMessageQueue(e, title + " doesn't have any stored streaks.");
            return;
        }

        Set<Long> showableUsers;
        if (params.getE().isFromGuild()) {
            showableUsers = db.getAll(params.getE().getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
            showableUsers.add(author);
        } else {
            showableUsers = Set.of(author);
        }


        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s's Top streaks", CommandUtil.escapeMarkdown(title)))
                .setThumbnail(CommandUtil.noImageUrl(validUrl))
                .setFooter(String.format("%s has a total of %d %s!", CommandUtil.escapeMarkdown(title), topStreaks.size(), CommandUtil.singlePlural(topStreaks.size(), "streak", "streaks")));

        AtomicInteger atomicInteger = new AtomicInteger(1);
        AtomicInteger positionCounter = new AtomicInteger(1);

        Function<GlobalStreakEntities, String> mapper = (x) -> {
            PrivacyUtils.PrivateString publicString = PrivacyUtils.getPublicString(x.getPrivacyMode(), x.getDiscordId(), x.getLastfmId(), atomicInteger, e, showableUsers);
            int andIncrement = positionCounter.getAndIncrement();
            String dayNumberSuffix = CommandUtil.getDayNumberSuffix(andIncrement);


            String aString = LinkUtils.cleanMarkdownCharacter(x.getCurrentArtist());
            StringBuilder description = new StringBuilder("%s **%s**%n".formatted(dayNumberSuffix, publicString.discordName()));
            GlobalStreakEntities.DateHolder holder = params.hasOptional("start") ? CommandUtil.toDateHolder(x.getStreakStart(), x.getLastfmId()) : null;

            return GlobalStreakEntities.getComboString(aString, description, x.artistCount(), x.getCurrentArtist(), x.albumCount(), x.getCurrentAlbum(), x.trackCount(), x.getCurrentSong(), holder);
        };

        new PaginatorBuilder<>(e, embedBuilder, topStreaks).memoized(mapper).pageSize(5).build().queue();
    }
}


