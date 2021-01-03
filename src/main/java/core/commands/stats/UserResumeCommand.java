package core.commands.stats;

import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.TimeFrameParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UserResumeCommand extends ConcurrentCommand<TimeFrameParameters> {
    public UserResumeCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<TimeFrameParameters> initParser() {
        return new TimerFrameParser(getService(), TimeFrameEnum.WEEK);
    }

    @Override
    public String getDescription() {
        return "User scrobble summary ";
    }

    @Override
    public List<String> getAliases() {
        return List.of("summary", "stats", "scrobbled");
    }

    @Override
    public String getName() {
        return "Scrobble Summary";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull TimeFrameParameters params) throws LastFmException, InstanceNotFoundException {

        LastFMData name = params.getLastFMData();
        BlockingQueue<UrlCapsule> capsules = new LinkedBlockingQueue<>();
        TimeFrameEnum time = params.getTime();


        CustomTimeFrame customTimeFrame = CustomTimeFrame.ofTimeFrameEnum(time);
        int albumCount = lastFM.getChart(name, customTimeFrame, 1, 1, TopEntity.ALBUM, ChartUtil.getParser(customTimeFrame, TopEntity.ALBUM, ChartParameters.toListParams(), lastFM, name), capsules);
        int artistCount = lastFM.getChart(name, customTimeFrame, 1, 1, TopEntity.ARTIST, ChartUtil.getParser(customTimeFrame, TopEntity.ARTIST, ChartParameters.toListParams(), lastFM, name), capsules);
        int trackCount = lastFM.getChart(name, customTimeFrame, 1, 1, TopEntity.TRACK, ChartUtil.getParser(customTimeFrame, TopEntity.TRACK, ChartParameters.toListParams(), lastFM, name), capsules);
        LocalDateTime localDateTime = time.toLocalDate(1);
        int i = lastFM.scrobblesSince(name, localDateTime.atOffset(ZoneOffset.UTC));
        DiscordUserDisplay info = CommandUtil.getUserInfoConsideringGuildOrNot(e, params.getLastFMData().getDiscordId());
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(info.getUsername() + "'s summary" + time.getDisplayString())
                .setColor(CommandUtil.randomColor())
                .setThumbnail(info.getUrlImage())
                .addField("Total scrobbles:", i + " scrobbles", false)
                .addField("Total songs:", trackCount + " songs", true)
                .addField("Total albums:", albumCount + " albums", true)
                .addField("Total artists:", artistCount + " artists", true);

        e.getChannel().sendMessage(new MessageBuilder().setEmbed(embedBuilder.build()).build()).queue();
    }
}
