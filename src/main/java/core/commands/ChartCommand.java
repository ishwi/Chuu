package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.CollageMaker;
import core.parsers.ChartParser;
import dao.ChuuService;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ChartCommand extends ConcurrentCommand {

    public ChartCommand(ChuuService dao) {

        super(dao);
        this.parser = new ChartParser(getService());
    }

    @Override
    public String getDescription() {
        return "Returns a Chart with albums";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("chart");
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned;
        returned = parser.parse(e);
        if (returned == null)
            return;

        int x = Integer.parseInt(returned[0]);
        int y = Integer.parseInt(returned[1]);
        String username = returned[2];
        String time = returned[3];
        boolean titleWrite = !Boolean.parseBoolean(returned[5]);
        boolean playsWrite = Boolean.parseBoolean(returned[6]);


        if (x * y > 100) {
            e.getChannel().sendMessage("Going to take a while").queue();
        }

        processQueue(username, time, x, y, e, titleWrite, playsWrite);


    }

    void processQueue(String username, String time, int x, int y, MessageReceivedEvent e, boolean writeTitles, boolean writePlays) throws LastFmException {
        BlockingQueue<UrlCapsule> queue = new LinkedBlockingDeque<>();
        lastFM.getUserList(username, time, x, y, true, queue);
        generateImage(queue, x, y, e, writeTitles, writePlays);
    }

    void generateImage(BlockingQueue<UrlCapsule> queue, int x, int y, MessageReceivedEvent e, boolean writeTitles, boolean writePlays) {
        int size = queue.size();
        ChartQuality chartQuality;
        int minx = (int) Math.ceil((double) size / x);
        //int miny = (int) Math.ceil((double) size / y);
        if (minx == 1)
            x = size;
        if (size > 45 && size < 400)
            chartQuality = ChartQuality.JPEG_BIG;
        else
            chartQuality = ChartQuality.JPEG_SMALL;
        BufferedImage image = CollageMaker
                .generateCollageThreaded(x, minx, queue, writeTitles, writePlays, chartQuality);
        sendImage(image, e, chartQuality);
    }

    @Override
    public String getName() {
        return "Chart";
    }


}
