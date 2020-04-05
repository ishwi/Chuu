package core.commands;

import core.apis.last.TopEntity;
import core.apis.last.chartentities.AlbumChart;
import core.apis.last.chartentities.ArtistChart;
import core.apis.last.chartentities.TrackChart;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.TimerFrameParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.TimeFrameParameters;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.TimeFrameEnum;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UserResumeCommand extends ConcurrentCommand {
    public UserResumeCommand(ChuuService dao) {
        super(dao);
        this.parser = new TimerFrameParser(dao, TimeFrameEnum.WEEK);
    }

    @Override
    public String getDescription() {
        return "User scrobble summary ";
    }

    @Override
    public List<String> getAliases() {
        return List.of("summary", "stats");
    }

    @Override
    public String getName() {
        return "Scrobble Summary";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        TimeFrameParameters tfP = new TimeFrameParameters(parse, e);
        BlockingQueue<UrlCapsule> capsules = new LinkedBlockingQueue<>();
        TimeFrameEnum time = tfP.getTime();
        int albumCount = lastFM.getChart(tfP.getLastFMId(), time.toApiFormat(), 1, 1, TopEntity.ALBUM, AlbumChart.getAlbumParser(ChartParameters.toListParams()), capsules);
        int artistCount = lastFM.getChart(tfP.getLastFMId(), time.toApiFormat(), 1, 1, TopEntity.ARTIST, ArtistChart.getArtistParser(ChartParameters.toListParams()), capsules);
        int trackCount = lastFM.getChart(tfP.getLastFMId(), time.toApiFormat(), 1, 1, TopEntity.TRACK, TrackChart.getTrackParser(ChartParameters.toListParams()), capsules);
        LocalDateTime localDateTime = time.toLocalDate(1);
        int i = lastFM.scrobblesSince(tfP.getLastFMId(), localDateTime.atOffset(ZoneOffset.UTC));
        EmbedBuilder embedBuilder = new EmbedBuilder();
        DiscordUserDisplay info = CommandUtil.getUserInfoConsideringGuildOrNot(e, tfP.getDiscordID());
        embedBuilder.setTitle(info.getUsername() + "'s summary" + time.getDisplayString())
                .setColor(CommandUtil.randomColor())
                .setThumbnail(info.getUrlImage())
                .addField("Total scrobbles:", i + " scrobbles", false)
                .addField("Total songs:", trackCount + " songs", true)
                .addField("Total albums:", albumCount + " albums", true)
                .addField("Total artists:", artistCount + " artists", true)
                .build();

        new MessageBuilder().setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
    }
}
