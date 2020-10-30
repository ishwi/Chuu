package core.commands;

import core.apis.last.TopEntity;
import core.apis.last.chartentities.ChartUtil;
import core.apis.last.chartentities.UrlCapsule;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.TimeFrameParameters;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<TimeFrameParameters> getParser() {
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        TimeFrameParameters tfP = parser.parse(e);
        if (tfP == null) {
            return;
        }
        String name = tfP.getLastFMData().getName();
        BlockingQueue<UrlCapsule> capsules = new LinkedBlockingQueue<>();
        TimeFrameEnum time = tfP.getTime();


        int albumCount = lastFM.getChart(name, time.toApiFormat(), 1, 1, TopEntity.ALBUM, ChartUtil.getParser(time, TopEntity.ALBUM, ChartParameters.toListParams(), lastFM, name), capsules);
        int artistCount = lastFM.getChart(name, time.toApiFormat(), 1, 1, TopEntity.ARTIST, ChartUtil.getParser(time, TopEntity.ARTIST, ChartParameters.toListParams(), lastFM, name), capsules);
        int trackCount = lastFM.getChart(name, time.toApiFormat(), 1, 1, TopEntity.TRACK, ChartUtil.getParser(time, TopEntity.TRACK, ChartParameters.toListParams(), lastFM, name), capsules);
        LocalDateTime localDateTime = time.toLocalDate(1);
        int i = lastFM.scrobblesSince(name, localDateTime.atOffset(ZoneOffset.UTC));
        EmbedBuilder embedBuilder = new EmbedBuilder();
        DiscordUserDisplay info = CommandUtil.getUserInfoConsideringGuildOrNot(e, tfP.getLastFMData().getDiscordId());
        embedBuilder.setTitle(info.getUsername() + "'s summary" + time.getDisplayString())
                .setColor(CommandUtil.randomColor())
                .setThumbnail(info.getUrlImage())
                .addField("Total scrobbles:", i + " scrobbles", false)
                .addField("Total songs:", trackCount + " songs", true)
                .addField("Total albums:", albumCount + " albums", true)
                .addField("Total artists:", artistCount + " artists", true)
                .build();

        e.getChannel().sendMessage(new MessageBuilder().setEmbed(embedBuilder.build()).build()).queue();
    }
}
