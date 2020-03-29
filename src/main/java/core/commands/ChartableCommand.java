package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.CollageMaker;
import core.otherlisteners.Reactionary;
import core.parsers.ChartParser;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public abstract class ChartableCommand extends ConcurrentCommand {

    public ChartableCommand(ChuuService dao) {
        super(dao);
        this.parser = new ChartParser(getService());
    }

    public ChartParameters getParameters(String[] message, MessageReceivedEvent e) {
        return new ChartParameters(message, e);
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned;
        returned = parser.parse(e);
        if (returned == null)
            return;
        ChartParameters chartParameters = getParameters(returned, e);
        CountWrapper<BlockingQueue<UrlCapsule>> countWrapper = processQueue(chartParameters);
        BlockingQueue<UrlCapsule> urlCapsules = countWrapper.getResult();
        if (urlCapsules.isEmpty()) {
            this.noElementsMessage(e, chartParameters);
            return;
        }
        if (chartParameters.isList()) {
            ArrayList<UrlCapsule> liste = new ArrayList<>(urlCapsules.size());
            urlCapsules.drainTo(liste);
            doList(liste, chartParameters, countWrapper.getRows());
        } else {
            doImage(urlCapsules, chartParameters.getX(), chartParameters.getY(), chartParameters);
        }
    }


    public abstract CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters params) throws LastFmException;

    void generateImage(BlockingQueue<UrlCapsule> queue, int x, int y, MessageReceivedEvent e) {
        int size = queue.size();
        ChartQuality chartQuality = ChartQuality.PNG_BIG;
        int minx = (int) Math.ceil((double) size / x);
        //int miny = (int) Math.ceil((double) size / y);
        if (minx == 1)
            x = size;
        if (size > 45 && size < 400)
            chartQuality = ChartQuality.JPEG_BIG;
        else if (size >= 400)
            chartQuality = ChartQuality.JPEG_SMALL;
        BufferedImage image = CollageMaker
                .generateCollageThreaded(x, minx, queue, chartQuality);
        sendImage(image, e, chartQuality);
    }


    public void doImage(BlockingQueue<UrlCapsule> queue, int x, int y, ChartParameters parameters) {
        CompletableFuture<Message> future = null;
        MessageReceivedEvent e = parameters.getE();
        if (x * y > 100) {
            future = e.getChannel().sendMessage("Going to take a while").submit();
        }
        generateImage(queue, x, y, e);
        CommandUtil.handleConditionalMessage(future);
    }


    public void doList(List<UrlCapsule> urlCapsules, ChartParameters params, int count) {

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < urlCapsules.size(); i++) {
            a.append(i + 1).append(urlCapsules.get(i).toEmbedDisplay());
        }
        DiscordUserDisplay userInfoConsideringGuildOrNot = CommandUtil.getUserInfoConsideringGuildOrNot(params.getE(), params.getDiscordId());

        EmbedBuilder embedBuilder = configEmbed(new EmbedBuilder()
                .setDescription(a)
                .setColor(CommandUtil.randomColor())
                .setThumbnail(userInfoConsideringGuildOrNot.getUrlImage()), params, count);
        MessageBuilder mes = new MessageBuilder();
        params.getE().getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                executor.execute(() -> new Reactionary<>(urlCapsules, message1, embedBuilder)));
    }

    public abstract EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count);

    public abstract void noElementsMessage(MessageReceivedEvent e, ChartParameters parameters);


}
