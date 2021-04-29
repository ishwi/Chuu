package core.commands.discovery;

import core.Chuu;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.PresenceInfo;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FeaturedCommand extends ConcurrentCommand<CommandParameters> {
    private static final String DEFAULT_URL = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/180902_%EC%8A%A4%EC%B9%B4%EC%9D%B4%ED%8E%98%EC%8A%A4%ED%8B%B0%EB%B2%8C_%EC%9D%B4%EB%8B%AC%EC%9D%98_%EC%86%8C%EB%85%80_yyxy.jpg/800px-180902_%EC%8A%A4%EC%B9%B4%EC%9D%B4%ED%8E%98%EC%8A%A4%ED%8B%B0%EB%B2%8C_%EC%9D%B4%EB%8B%AC%EC%9D%98_%EC%86%8C%EB%85%80_yyxy.jpg";
    private static final String DEFAULT_ARTIST = "LOOΠΔ";
    private static final String DEFAULT_USER = "Chuu";

    private PresenceInfo currentPresence;

    public FeaturedCommand(ChuuService dao, ScheduledExecutorService scheduledManager) {
        super(dao);
        currentPresence = new PresenceInfo(DEFAULT_ARTIST, DEFAULT_URL, Long.MAX_VALUE, 1);
        scheduledManager.scheduleAtFixedRate(() -> {
            try {
                PresenceInfo presenceInfo;

                presenceInfo = db.getRandomArtistWithUrl();
                Chuu.updatePresence(presenceInfo.getArtist());
                this.currentPresence = presenceInfo;
                Chuu.getLogger()
                        .info("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE) + "]\t!Updated Presence");
            } catch (Exception e) {
                Chuu.getLogger().warn(e.getMessage());
            }
        }, 1, 30, TimeUnit.MINUTES);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_INFO;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Info about the artist that appears on the bot status";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("featured");
    }

    @Override
    public String getName() {
        return "Featured Artist";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) {
        String userString = this.getUserString(e, currentPresence.getDiscordId(), DEFAULT_USER);
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(ColorService.computeColor(e))
                .setThumbnail(CommandUtil.noImageUrl(currentPresence.getUrl()))
                .setTitle(e.getJDA().getSelfUser().getName() + "'s Featured Artist:", LinkUtils
                        .getLastFmArtistUrl(currentPresence.getArtist()))
                .addField("Artist:", CommandUtil.cleanMarkdownCharacter(currentPresence.getArtist()), false)
                .addField("User:", userString, false)
                .addField("Total Artist Plays:", String.valueOf(currentPresence.getSum()), false);

        e.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
