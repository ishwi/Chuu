package core.commands.streaks;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.utils.Optionals;
import core.util.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.GlobalStreakEntities;
import dao.entities.StreakEntity;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


public class MyCombosCommand extends ConcurrentCommand<ChuuDataParams> {
    public MyCombosCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.STREAKS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db, Optionals.START.opt);
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
    public void onCommand(Context e, @Nonnull ChuuDataParams params) {


        long discordID = params.getLastFMData().getDiscordId();
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoEscaped(e, discordID);
        String userName = userInformation.username();
        String userUrl = userInformation.urlImage();
        List<StreakEntity> userStreaks = db.getUserStreaks(discordID);

        if (userStreaks.isEmpty()) {
            sendMessageQueue(e, userName + " doesn't have any stored streak in the bot.");
            return;
        }

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s's streaks", CommandUtil.unescapedUser(userName, discordID, e)), CommandUtil.getLastFmUser(params.getLastFMData().getName()), userUrl)
                .setThumbnail(CommandUtil.noImageUrl(userUrl))
                .setFooter(String.format("%s has a total of %d %s!", CommandUtil.unescapedUser(userName, discordID, e), userStreaks.size(), CommandUtil.singlePlural(userStreaks.size(), "streak", "streaks")));

        AtomicInteger atomicInteger = new AtomicInteger(1);
        Function<StreakEntity, String> mapper = combo -> {
            String aString = CommandUtil.escapeMarkdown(combo.getCurrentArtist());
            int andIncrement = atomicInteger.getAndIncrement();
            StringBuilder description = new StringBuilder(CommandUtil.getDayNumberSuffix(andIncrement) + "\n");
            GlobalStreakEntities.DateHolder holder = params.hasOptional("start") ? CommandUtil.toDateHolder(combo.getStreakStart(), params.getLastFMData().getName()) : null;

            return GlobalStreakEntities.getComboString(aString, description, combo.artistCount(), combo.getCurrentArtist(), combo.albumCount(), combo.getCurrentAlbum(), combo.trackCount(), combo.getCurrentSong(), holder);
        };

        new PaginatorBuilder<>(e, embedBuilder, userStreaks).mapper(mapper).pageSize(5).build().queue();

    }
}
