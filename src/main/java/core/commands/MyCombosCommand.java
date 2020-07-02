package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.GlobalStreakEntities;
import dao.entities.StreakEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class MyCombosCommand extends ConcurrentCommand<ChuuDataParams> {
    public MyCombosCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> getParser() {
        return new OnlyUsernameParser(getService());
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ChuuDataParams params = parser.parse(e);
        if (params == null) {
            return;
        }
        Long discordID = params.getLastFMData().getDiscordId();
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordID);
        String userName = userInformation.getUsername();
        String userUrl = userInformation.getUrlImage();
        List<StreakEntity> userStreaks = getService().getUserStreaks(discordID);
        AtomicInteger atomicInteger = new AtomicInteger(1);
        List<String> streaks = userStreaks
                .stream().map(combo -> {
                            String aString = CommandUtil.cleanMarkdownCharacter(combo.getCurrentArtist());
                            int andIncrement = atomicInteger.getAndIncrement();
                            StringBuilder description = new StringBuilder(CommandUtil.getDayNumberSuffix(andIncrement) + "\n");
                            return GlobalStreakEntities.getComboString(aString, description, combo.getaCounter(), combo.getCurrentArtist(), combo.getAlbCounter(), combo.getCurrentAlbum(), combo.gettCounter(), combo.getCurrentSong());
                        }
                ).collect(Collectors.toList());
        if (streaks.isEmpty()) {
            sendMessageQueue(e, userName + " doesn't have any stored streak in the bot.");
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 5 && i < streaks.size(); i++) {
            a.append(i + 1).append(streaks.get(i));
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(String.format("%s's streaks", CommandUtil.markdownLessUserString(userName, discordID, e)), CommandUtil.getLastFmUser(params.getLastFMData().getName()), userUrl)
                .setThumbnail(CommandUtil.noImageUrl(userUrl))
                .setDescription(a)
                .setFooter(String.format("%s has a total of %d %s!", CommandUtil.markdownLessUserString(userName, discordID, e), streaks.size(), CommandUtil.singlePlural(streaks.size(), "streak", "streaks")));
        MessageBuilder messageBuilder = new MessageBuilder();
        e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(userStreaks, message1, 5, embedBuilder));
    }
}
