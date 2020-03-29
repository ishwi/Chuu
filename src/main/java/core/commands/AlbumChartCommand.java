package core.commands;

import core.apis.last.TopEntity;
import core.apis.last.chartentities.AlbumChart;
import core.exceptions.LastFmException;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AlbumChartCommand extends ChartableCommand {

    public AlbumChartCommand(ChuuService dao) {

        super(dao);

    }

    @Override
    public String getDescription() {
        return "Returns a chart with album images";
    }

    @Override
    public List<String> getAliases() {
        return List.of("chart", "c");
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters param) throws LastFmException {
        BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
        int i = param.makeCommand(lastFM, queue, TopEntity.ALBUM, AlbumChart.getAlbumParser(param));
        return new CountWrapper<>(i, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        return params.initEmbed("'s top albums", embedBuilder, " has listened to " + count + " albums");
    }

    @Override
    public void noElementsMessage(MessageReceivedEvent e, ChartParameters parameters) {
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any album%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }


}
