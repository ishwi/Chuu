package core.commands.stats;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.GlobalStreakEntities;
import dao.entities.StreakEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class MyCombosCommand extends ConcurrentCommand<ChuuDataParams> {
    public MyCombosCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db, new OptionalEntity("start", "show the moment the streak started"));
    }

    @Override
    public String getDescription() {
        return "Check what are your longest streaks to date. Only combos with more than 20 plays are stored";
    }

    @Override
    public List<String> getAliases() {
        return List.of("mycombos", "combos", "streaks");
    }

    @Override
    public String getName() {
        return "My Combos";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) {


        Long discordID = params.getLastFMData().getDiscordId();
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordID);
        String userName = userInformation.getUsername();
        String userUrl = userInformation.getUrlImage();
        List<StreakEntity> userStreaks = db.getUserStreaks(discordID);
        AtomicInteger atomicInteger = new AtomicInteger(1);
        List<String> streaks = userStreaks
                .stream().map(combo -> {
                            String aString = CommandUtil.cleanMarkdownCharacter(combo.getCurrentArtist());
                            int andIncrement = atomicInteger.getAndIncrement();
                            StringBuilder description = new StringBuilder(CommandUtil.getDayNumberSuffix(andIncrement) + "\n");
                            GlobalStreakEntities.DateHolder holder = params.hasOptional("start") ? CommandUtil.toDateHolder(combo.getStreakStart(), params.getLastFMData().getName()) : null;

                            return GlobalStreakEntities.getComboString(aString, description, combo.getaCounter(), combo.getCurrentArtist(), combo.getAlbCounter(), combo.getCurrentAlbum(), combo.gettCounter(), combo.getCurrentSong(), holder);
                        }
                ).collect(Collectors.toCollection(ArrayList::new));
        if (streaks.isEmpty()) {
            sendMessageQueue(e, userName + " doesn't have any stored streak in the bot.");
            return;
        }

        int maxLength = streaks.stream().mapToInt(String::length).max().orElse(0);
        int j = 0;
        while (j < streaks.size()) {
            int size = 0;
            for (int i = j; i < 5 + j && i < streaks.size(); i++) {
                size += streaks.get(i).length();
            }
            if (size >= 2048) {
                for (int i = j; i < 5 + j && i < streaks.size(); i++) {
                    streaks.set(i, streaks.get(i).replaceAll("\\[(.*)]\\(.*\\)", "$1"));
                }
            }
            j += 5;
        }


        StringBuilder a = new StringBuilder();
        AtomicInteger maxSize = new AtomicInteger(0);
        for (int i = 0; i < 5 && i < streaks.size(); i++) {
            String str = streaks.get(i);
            if (a.length() + str.length() > 2048) {
                break;
            }
            a.append(i + 1).append(str);
            maxSize.incrementAndGet();
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(String.format("%s's streaks", CommandUtil.markdownLessUserString(userName, discordID, e)), CommandUtil.getLastFmUser(params.getLastFMData().getName()), userUrl)
                .setThumbnail(CommandUtil.noImageUrl(userUrl))
                .setColor(ColorService.computeColor(e))
                .setDescription(a)
                .setFooter(String.format("%s has a total of %d %s!", CommandUtil.markdownLessUserString(userName, discordID, e), streaks.size(), CommandUtil.singlePlural(streaks.size(), "streak", "streaks")));
        e.getChannel().sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(streaks, message1, maxSize.get(), embedBuilder));
    }
}
