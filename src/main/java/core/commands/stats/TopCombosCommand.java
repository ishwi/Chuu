package core.commands.stats;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.GlobalStreakEntities;
import dao.entities.PrivacyMode;
import dao.entities.UsersWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class TopCombosCommand extends ConcurrentCommand<NumberParameters<CommandParameters>> {

    public TopCombosCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce a number to only get streak with more than that number of plays. ";
        NumberParser<CommandParameters, NoOpParser> parser = new NumberParser<>(new NoOpParser(),
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true);
        parser.addOptional(new OptionalEntity("server", "only include people in this server"));
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
    protected void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<CommandParameters> params) {

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
        List<GlobalStreakEntities> topStreaks = getService().getTopStreaks(params.getExtraParam(), guildId);

        Set<Long> showableUsers;
        if (params.getE().isFromGuild()) {
            showableUsers = getService().getAll(params.getE().getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
            showableUsers.add(author);
        } else {
            showableUsers = Set.of(author);
        }
        AtomicInteger atomicInteger = new AtomicInteger(1);
        AtomicInteger positionCounter = new AtomicInteger(1);

        Consumer<GlobalStreakEntities> consumer = (x) -> {
            PrivacyMode privacyMode = x.getPrivacyMode();
            if (showableUsers.contains(x.getDiscordId())) {
                privacyMode = PrivacyMode.DISCORD_NAME;
            }
            int andIncrement = positionCounter.getAndIncrement();
            String dayNumberSuffix = CommandUtil.getDayNumberSuffix(andIncrement);
            switch (privacyMode) {
                case STRICT, NORMAL -> x.setCalculatedDisplayName(dayNumberSuffix + " **Private User #" + atomicInteger.getAndIncrement() + "**");
                case DISCORD_NAME -> x.setCalculatedDisplayName(dayNumberSuffix + " **" + getUserString(params.getE(), x.getDiscordId()) + "**");
                case TAG -> x.setCalculatedDisplayName(dayNumberSuffix + " **" + params.getE().getJDA().retrieveUserById(x.getDiscordId()).complete().getAsTag() + "**");
                case LAST_NAME -> x.setCalculatedDisplayName(dayNumberSuffix + " **" + x.getLastfmId() + " (last.fm)**");
            }

        };
        topStreaks
                .forEach(x ->
                        x.setDisplayer(consumer)
                );
        atomicInteger.set(0);
        topStreaks.forEach(x -> x.setDisplayer(consumer));
        if (topStreaks.isEmpty()) {
            sendMessageQueue(e, title + " doesn't have any stored streaks.");
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 5 && i < topStreaks.size(); i++) {
            a.append(i + 1).append(topStreaks.get(i).toString());
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .
                        setAuthor(String.format("%s's Top streaks", CommandUtil.cleanMarkdownCharacter(title)))
                .setThumbnail(CommandUtil.noImageUrl(validUrl))
                .setDescription(a)
                .setFooter(String.format("%s has a total of %d %s!", CommandUtil.cleanMarkdownCharacter(title), topStreaks.size(), CommandUtil.singlePlural(topStreaks.size(), "streak", "streaks")));
        e.getChannel().sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(topStreaks, message1, 5, embedBuilder));
    }
}


